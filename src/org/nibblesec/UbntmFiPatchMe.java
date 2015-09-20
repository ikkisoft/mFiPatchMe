/*
 * UbntmFiPatchMe v0.1 - 09/19/2015 - @_ikki
 *
 * Unofficial security patch for Ubiquiti mFi Controller 2.1.11. Use at your own risk!
 * 
 * This code will patch a recent Ubiquiti Networks mFi Controller Server Authentication Bypass 
 * For more details, please refer to http://blogs.securiteam.com/index.php/archives/2580
 */
package org.nibblesec;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public class UbntmFiPatchMe {

    private static final String version = "v0.1";

    private static void help() {
        System.out.println("\nUnofficial security patch for Ubiquiti mFi Controller 2.1.11\n");
        System.out.println("Usage:");
        System.out.println(" java -jar UbntmFiPatchMe.jar -file <ace.jar>\n");
        System.exit(-1);
    }

    public static void main(String[] args) {
        try {
            System.out.println("\n--[ UbntmFiPatchMe " + version + " - @_ikki ]");

            if (args.length < 1) {
                help();
            } else if (args[0].equals("-file")) {
                String mfiJar = args[1];
                if (mfiJar.contains("ace.jar") && mfiJar.substring(mfiJar.length() - 4).equalsIgnoreCase(".jar")) {

                    System.out.println("  [*] Opening Ubiquiti mFi Controller's jar \"" + mfiJar + "\"");
                    JarFile myJar = new JarFile(mfiJar);

                    Enumeration<JarEntry> entries = myJar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();
                        if (entryName.endsWith(".class")) {
                            ClassNode classNode = new ClassNode();
                            InputStream classFileInputStream = myJar.getInputStream(entry);
                            try {
                                ClassReader classReader = new ClassReader(classFileInputStream);
                                classReader.accept((ClassVisitor) classNode, 0);
                            } finally {
                                classFileInputStream.close();
                            }

                            if (classNode.name.equalsIgnoreCase("com/ubnt/ace/view/AuthFilter")) {
                                System.out.println("  [*] Patching \"" + classNode.name + "\" class");

                                ClassPool cp = ClassPool.getDefault();
                                URL[] classUrls = new URL[1];
                                classUrls[0] = (new File(mfiJar)).toURI().toURL();
                                URLClassLoader ucl = new URLClassLoader(classUrls);
                                ClassPath jarCp = new LoaderClassPath(ucl);
                                cp.insertClassPath(jarCp);
                                CtClass afilter = cp.get(classNode.name.replaceAll("/", "."));
                                CtMethod mm = afilter.getDeclaredMethod("doFilter");
                                mm.instrument(
                                        new ExprEditor() {
                                            public void edit(MethodCall m)
                                            throws CannotCompileException {
                                                if (m.getClassName().equals("javax.servlet.http.HttpServletRequest")
                                                && m.getMethodName().equals("getRequestURI")) {
                                                    m.replace("{$_ = java.net.URLDecoder.decode($proceed($$),\"UTF-8\").replace(\"../\", \"\").replace(\"..\", \"\").replace(\"./\", \"\").replace(\"//\", \"/\");}");
                                                }
                                            }
                                        });

                                File tempDir = Files.createTempDir();
                                afilter.writeFile(tempDir.getAbsolutePath());
                                Scanner user_input = new Scanner(System.in);
                                System.out.print("  [*] Ready to update \"" + mfiJar + "\". Are you sure? (yes/no)\n  >");
                                String confirmation = user_input.next();

                                if (confirmation.equalsIgnoreCase("yes")) {
                                    String[] cmdArray = new String[6];
                                    cmdArray[0] = "jar";
                                    cmdArray[1] = "-uvf";
                                    cmdArray[2] = mfiJar;
                                    cmdArray[3] = "-C";
                                    cmdArray[4] = tempDir.getAbsolutePath();
                                    cmdArray[5] = classNode.name + ".class";
                                    System.out.println("  [*] Executing \"jar -uvf " + mfiJar + " -C " + tempDir.getAbsolutePath() + " " + classNode.name + ".class\"");
                                    Process process = Runtime.getRuntime().exec(cmdArray);
                                    process.waitFor();
                                    if (process.exitValue() != 0) {
                                        System.out.print("  [!] An error occurred while updating the original jar");
                                    }
                                    tempDir.deleteOnExit();
                                    System.out.println("  [*] Patched");
                                } else {
                                    System.out.println("  [*] If you care, the patched class is \"" + tempDir + File.separator + classNode.name + ".class\"");
                                }
                                System.exit(0);
                            }
                        }
                    }
                }
                //we shouldn't be here
                System.out.println("  [!] UbntmFiPatchMe Exception");
                System.out.println("  [!] Have you specified the right .jar file?");
                System.exit(-1);
            } else {
                help();
            }
        } catch (IOException ex) {
            System.out.println("  [!] IOException: " + ex.toString());
        } catch (NotFoundException ex) {
            System.out.println("  [!] NotFoundException: " + ex.toString());
        } catch (CannotCompileException ex) {
            System.out.println("  [!] CannotCompileException: " + ex.toString());
        } catch (InterruptedException ex) {
            System.out.println("  [!] InterruptedException: " + ex.toString());
        }
    }
}
