import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {

		Scanner reader = new Scanner(System.in);
		System.out.println("Enter the name of the source folder to compile: ");
		String sourceFolder = reader.nextLine();
		String mainClass = null;
		System.out.println("Enter the desired name of the application: ");
		String variationName = reader.nextLine();
		reader.close();

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		List<File> files = listf("SolitaireSource" + java.io.File.separator + sourceFolder, ".java");

		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

		List<String> options = Arrays.asList("-d", System.getProperty("user.dir"), "-cp", "standAlone.jar");

		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null,
				compilationUnits);

		try {
			if (!task.call()) {
				for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
					System.err.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
				}
			} else
				System.out.println("Compilation Successful");

			List<File> compiledFiles = listf("org", ".class");
			
			File standAlone = new File("standAlone.jar");
			URLClassLoader saLoader = new URLClassLoader(new URL[] {standAlone.toURI().toURL()});

			Class clsReaderClss = ClassLoader.getSystemClassLoader()
					.loadClass("jdk.internal.org.objectweb.asm.ClassReader");
			try {
				Constructor con = clsReaderClss.getConstructor(InputStream.class);
				Method getClassNameMethod = clsReaderClss.getMethod("getClassName");

				for (File f : compiledFiles) {
					URL url = f.toURI().toURL();
					String classname = null;

					try {
						Object classNameReader = con.newInstance(new FileInputStream(url.getPath()));
						classname = getClassNameMethod.invoke(classNameReader).toString().replace('/', '.');
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (classname != null) {
						String pathToPackageBase = url.getPath().substring(0, url.getPath().length() - (classname + ".class").length());
						URLClassLoader cl = new URLClassLoader(new URL[]{new File(pathToPackageBase).toURI().toURL()}, saLoader);
						Class clazz = cl.loadClass(classname);
						
						try {
							clazz.getMethod("main", String[].class);
							mainClass = clazz.getName();
						} catch (NoSuchMethodException e) {
						}
						
						cl.close();
					}
				}

			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
			saLoader.close();
			System.out.println(mainClass + " is main class");

			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
			manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, "shared/standAlone.jar");
			JarOutputStream target = new JarOutputStream(new FileOutputStream(variationName + ".jar"), manifest);
			add(new File("org"), target);
			target.close();

			fileManager.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	private static List<File> listf(String directoryName, String ext) {
		File directory = new File(directoryName);

		List<File> resultList = new ArrayList<File>();

		// get all the files from a directory
		File[] fList = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(ext);
			}
		});
		resultList.addAll(Arrays.asList(fList));
		fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
			} else if (file.isDirectory()) {
				resultList.addAll(listf(file.getAbsolutePath(), ext));
			}
		}
		return resultList;
	}

	private static void add(File source, JarOutputStream target) throws IOException {
		BufferedInputStream in = null;
		try {
			if (source.isDirectory()) {
				String name = source.getPath().replace("\\", "/");
				if (!name.isEmpty()) {
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				for (File nestedFile : source.listFiles())
					add(nestedFile, target);
				return;
			}

			JarEntry entry = new JarEntry(source.getPath().replace("\\", "/"));
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));

			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		} finally {
			if (in != null)
				in.close();
		}
	}
}
