package org.walkmod;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class WalkmodDispatcherTest extends AbstractWalkmodExecutionTest {

    @Test
    public void testNoArgs() throws Exception {
        Assert.assertTrue(run(null).contains("walkmod COMMAND [arg...]"));
    }

    @Test
    public void testUsage() throws Exception {
        Assert.assertTrue(run(new String[] { "--help" }).contains("walkmod COMMAND [arg...]"));
    }

    @Test
    public void testApply() throws Exception {
        String aux = run(new String[] { "apply", "-e" });
        System.out.println(aux);
        Assert.assertTrue(aux.contains("TRANSFORMATION CHAIN SUCCESS"));
    }

    @Test
    public void testApplyWithParams() throws Exception {
        Assert.assertTrue(run(new String[] { "apply", "--includes",
                new File("src/main/java/org/walkmod/WalkmodFacade.java").getAbsolutePath() })
                        .contains("TRANSFORMATION CHAIN SUCCESS"));
    }

    @Test
    public void testInstall() throws Exception {
        Assert.assertTrue(run(new String[] { "install" }).contains("PLUGIN INSTALLATION COMPLETE"));

    }

    @Test
    public void testCheck() throws Exception {
        Assert.assertTrue(run(new String[] { "check" }).contains("TRANSFORMATION CHAIN SUCCESS"));

    }

    @Test
    public void testCheckWithParams() throws Exception {
        Assert.assertTrue(run(new String[] { "check", "--includes",
                new File("src/main/java/org/walkmod/WalkmodFacade.java").getAbsolutePath() })
                        .contains("TRANSFORMATION CHAIN SUCCESS"));
    }

    @Test
    public void testVersion() throws Exception {
        Assert.assertTrue(run(new String[] { "--version" }).contains("Walkmod version"));
    }

    @Test
    public void testInvalidGoal() throws Exception {
        Assert.assertTrue(run(new String[] { "foo" }).contains("Expected a command, got foo"));
    }

    @Test
    public void testApplyWithInvalidArgs() throws Exception {
        Assert.assertTrue(run(new String[] { "apply", "-F" }).contains("Unknown option: -F"));
    }

    @Test
    public void testPrintPlugins() throws Exception {
        Assert.assertTrue(run(new String[] { "plugins" }).contains("javalang"));
    }

    @Test
    public void testInitWithExistingCfgFile() throws Exception {
        Assert.assertTrue(run(new String[] { "init", "-f", "xml" }).contains("already exists"));
    }

    @Test
    public void testInitWithNonExistingCfgFile() throws Exception {

        File tmp = new File("src/test/resources/initTest");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                Assert.assertTrue(run(new String[] { "init", "-f", "xml" }).contains("walkmod.xml] CREATION COMPLETE"));
            } finally {
                System.setProperty("user.dir", userDir);
                FileUtils.deleteDirectory(tmp);
            }

        }

    }

    @Test
    public void testAddPlugin() throws Exception {
        File tmp = new File("src/test/resources/initTestAddPlugin");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                run(new String[] { "add-plugin", "org.walkmod:walkmod-imports-cleaner-plugin:2.0" });
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertFalse(false);
            } finally {
                System.setProperty("user.dir", userDir);

                File cfg = new File(tmp, "walkmod.xml");
                Assert.assertTrue(cfg.exists());

                String content = FileUtils.readFileToString(cfg);

                System.out.println(content);

                Assert.assertTrue(content.contains("walkmod-imports-cleaner-plugin"));

                FileUtils.deleteDirectory(tmp);
            }

        }
    }

    @Test
    public void testTranformationPlugin() throws Exception {
        File tmp = new File("src/test/resources/initTestAddTrans");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                run(new String[] { "add", "imports-cleaner" });
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertFalse(false);
            } finally {
                System.setProperty("user.dir", userDir);

                File cfg = new File(tmp, "walkmod.xml");
                Assert.assertTrue(cfg.exists());

                String content = FileUtils.readFileToString(cfg);

                System.out.println(content);

                Assert.assertTrue(content.contains("imports-cleaner"));

                FileUtils.deleteDirectory(tmp);
            }

        }
    }

    @Ignore
    public void testPatchWithoutConfig() throws Exception {
        File tmp = new File("src/test/resources/multimodulewithoutconfig");
        String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
        System.setProperty("user.dir", tmp.getAbsolutePath());
        try {
            run(new String[] { "patch", "-o", "-g", "-s", "json", "-p", ".",
                    "-DconfigurationFile=rulesets/java/controversial.xml", "-DsplitExecution=true", "-e", "--offline",
                    "pmd" });
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertFalse(false);
        } finally {
            System.setProperty("user.dir", userDir);
            File cfg = new File(tmp, "walkmod.xml");
            Assert.assertFalse(cfg.exists());
            File patch = new File(new File(tmp, "module1"), "walkmod.patch");
            Assert.assertTrue(patch.exists());
            patch = new File(new File(tmp, "module2"), "walkmod.patch");
            Assert.assertTrue(patch.exists());
            patch = new File(tmp, "walkmod.patch");
            Assert.assertFalse(patch.exists());
        }

    }

    @Test
    public void testAddProvider() throws Exception {
        File tmp = new File("src/test/resources/initTestAddProv");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                run(new String[] { "add-provider", "maven" });
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertFalse(false);
            } finally {
                System.setProperty("user.dir", userDir);

                File cfg = new File(tmp, "walkmod.xml");
                Assert.assertTrue(cfg.exists());

                String content = FileUtils.readFileToString(cfg);

                System.out.println(content);

                Assert.assertTrue(content.contains("maven"));

                FileUtils.deleteDirectory(tmp);
            }

        }
    }

    @Test
    public void testModule() throws Exception {
        File tmp = new File("src/test/resources/addModule");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                run(new String[] { "add-module", "module1" });
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertFalse(false);
            } finally {
                System.setProperty("user.dir", userDir);

                File cfg = new File(tmp, "walkmod.xml");
                Assert.assertTrue(cfg.exists());

                String content = FileUtils.readFileToString(cfg);

                System.out.println(content);

                Assert.assertTrue(content.contains("module1"));

                FileUtils.deleteDirectory(tmp);
            }

        }
    }

    @Test
    public void testRemoveTranf() throws Exception {
        File tmp = new File("src/test/resources/rmTransf");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                run(new String[] { "add", "imports-cleaner" });
                run(new String[] { "rm", "imports-cleaner" });
            } catch (Exception e) {
                e.printStackTrace();
                Assert.assertFalse(false);
            } finally {
                System.setProperty("user.dir", userDir);

                File cfg = new File(tmp, "walkmod.xml");
                Assert.assertTrue(cfg.exists());

                String content = FileUtils.readFileToString(cfg);

                FileUtils.deleteDirectory(tmp);

                System.out.println(content);

                Assert.assertTrue(!content.contains("imports-cleaner"));

            }

        }
    }

    @Test
    public void testSetWriter() throws Exception {
        File tmp = new File("src/test/resources/setWriter");
        tmp.mkdirs();
        if (tmp.exists()) {
            String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
            System.setProperty("user.dir", tmp.getAbsolutePath());
            try {
                run(new String[] { "add", "imports-cleaner" });
                run(new String[] { "set-writer", "javalang:string-writer" });

                File cfg = new File("walkmod.xml").getAbsoluteFile();
                Assert.assertTrue(cfg.exists());

                String content = FileUtils.readFileToString(cfg);

                System.out.println(content);

                cfg.delete();

                Assert.assertTrue(content.contains("javalang:string-writer"));
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                System.setProperty("user.dir", userDir);

            }

        }
    }

    @Test
    public void testChains() throws Exception {

        run(new String[] { "chains" });

    }

    @Test
    public void testScript() throws Exception {
        File tmp = new File("src/test/resources/add-script").getAbsoluteFile();
        tmp.mkdirs();
        File walkmodCfg = new File(tmp, "walkmod.xml");
        walkmodCfg.delete();
        String scriptPath = new File("src/test/resources/scripts/myscript.groovy").getAbsolutePath();
        String code = "public class Foo { public String bar; }";
        File srcDir = new File(tmp, "src/main/java");
        srcDir.mkdirs();
        File srcFile = new File(srcDir, "Foo.java").getAbsoluteFile();
        FileUtils.write(srcFile, code);

        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        System.setProperty("user.dir", tmp.getAbsolutePath());

        try {
            run(new String[] { "add", "--name=private-fields", "-Dlocation=" + scriptPath, "script" });
            run(new String[] { "apply" });
            String newContent = FileUtils.readFileToString(srcFile);
            Assert.assertTrue(newContent.contains("private String"));
        } finally {
            System.setProperty("user.dir", userDir);
        }

    }

    public void testTemplate() throws Exception {
        File tmp = new File("src/test/resources/add-template").getCanonicalFile();
        tmp.mkdirs();
        File walkmodCfg = new File(tmp, "walkmod.xml");
        walkmodCfg.delete();

        String code = "package foo; public class Bar { public String bar; }";
        File srcDir = new File(tmp, "src/main/java/foo");
        srcDir.mkdirs();
        File srcFile = new File(srcDir, "Foo.java").getAbsoluteFile();
        FileUtils.write(srcFile, code);
        String templateFile = new File(tmp, "jpa-id.groovy").getAbsolutePath();

        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        System.setProperty("user.dir", tmp.getAbsolutePath());

        try {

            run(new String[] { "add", "--isMergeable", "--name=jpa-id", "-Dtemplates=[\"" + templateFile + "\"]",
                    "template" });
            String aux = run(new String[] { "apply", "-e" });
            System.out.println(aux);
            String newContent = FileUtils.readFileToString(srcFile);
            System.out.println(newContent);
            Assert.assertTrue(newContent.contains("@Id"));
        } finally {
            System.setProperty("user.dir", userDir);
        }

    }

    @Test
    public void testAddMvnConfProviderWithExtraArgs() throws Exception {
        File tmp = new File("src/test/resources/add-mvn").getCanonicalFile();
        tmp.mkdirs();
        File walkmodCfg = new File(tmp, "walkmod.xml");
        walkmodCfg.delete();
        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        System.setProperty("user.dir", tmp.getAbsolutePath());
        try {
            run(new String[] { "add-provider", "-DmavenArgs=\"-Dversion=11.0 -Drelease=0\"", "maven" });
        } finally {
            System.setProperty("user.dir", userDir);
        }
        String cfg = FileUtils.readFileToString(walkmodCfg);
        Assert.assertTrue(cfg.contains("mavenArgs"));
    }

    @Test
    public void testDifferentDirectories() throws Exception {
        File tmp = new File("src/test/resources/different-folders").getCanonicalFile();
        File producedOutput = new File(tmp, "src/main/java-out/Foo.java");
        if (producedOutput.exists()) {
            producedOutput.delete();
        }

        String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
        System.setProperty("user.dir", tmp.getAbsolutePath());
        String code = "";
        try {
            code = run(new String[] { "apply", "-e" });
        } finally {
            System.setProperty("user.dir", userDir);
        }
        producedOutput = producedOutput.getCanonicalFile();

        System.out.println(code);
        Assert.assertTrue(producedOutput.exists());
        producedOutput.delete();
    }

}
