import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panye on 2014/10/31.
 */
public class Main {
    public static void main(String[] args) throws MalformedURLException {
//        ELProcessor elp = new ELProcessor();
//        elp.setValue("a", "12.0%");
//        System.out.println(elp.eval("'12.0%' < '2%'"));
        new Main().test();

    }

    private void test() throws MalformedURLException {
        Map mm = new HashMap<>();
        File file1 = new File("D://test-classes");

        AClassLoader aClassLoader = new AClassLoader(new URL[]{file1.toURI().toURL()},
                Main.class.getClassLoader());
        try {
            Class<?> parent = Class.forName("com.test.dds.MM", true, aClassLoader);

            File file = new File("D://test-classes-1");

            PClassLoader pClassLoader = new PClassLoader(new URL[]{file.toURI().toURL()},
                    aClassLoader);

            Class<?> cl1 = Class.forName("com.test.dds.MM1", true, pClassLoader);
            parent.cast(cl1.newInstance());
            System.out.println(parent.getClassLoader());
            System.out.println(cl1.getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    class AClassLoader extends URLClassLoader {
        public AClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
    }
    class PClassLoader extends URLClassLoader {
        public PClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
    }

}
