/**
 * 
 */
package analyze;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugCollectionBugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugReporterObserver;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.PluginException;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.StringAnnotation;
import edu.umd.cs.findbugs.classfile.analysis.MethodInfo;
import edu.umd.cs.findbugs.config.UserPreferences;

/**
 * @author sdienst
 *
 */
public class AnalyzeAndroidApps {

	private static final class IntentStringObserver implements
			BugReporterObserver {
		String intent = null;

		@Override
		public void reportBug(BugInstance inst) {
			BugAnnotation bugAnnotation = inst.getAnnotations().get(0);
			if(bugAnnotation instanceof StringAnnotation){
				// might be ClassAnnotation if the analyzed class was too big...
				StringAnnotation annotation = (StringAnnotation) bugAnnotation;
				intent=annotation.getValue();
			}
		}

		public String getIntent(){ return intent;}
	}
	private static class BugCounterObserver implements BugReporterObserver{
		AtomicInteger counter = new AtomicInteger(0);
		@Override
		public void reportBug(BugInstance arg0) {
			counter.incrementAndGet();
		}
		public int getBugCount(){
			return counter.get();
		}
		
	}
	
	/**
	 * @param args
	 * @throws PluginException 
	 * @throws InterruptedException 
	 * @throws IOException
	 */
	public static void main(String[] args) throws PluginException, IOException, InterruptedException {
		
		ArrayList<String> apps = new ArrayList<String>();
		
		FileReader reader = new FileReader("apps.txt");
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		
		while((line = br.readLine()) != null) {
            apps.add(line);
        }
		br.close();
		reader.close();

        int count = 1;
		String root = "/Users/wind/workspace/network/apps/com.taobao.taobao/";
		
		for (String app: apps) {

			System.out.println(count + ". intents extrating ... ---> " + app);
            count++;

			String appDir = root + app + "/";

			FileWriter writer = null;
			BufferedWriter bw = null;

			try {
				File jar = new File(appDir + "classes.jar");

				writer = new FileWriter(appDir + "intents.txt");
				bw  = new BufferedWriter(writer);

				bw.write(findIntents(jar) + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bw != null) bw.close();
				if (writer != null) writer.close();
			}

		}

	}
	
	public static int countIntentConstructors(File jarfile) throws PluginException, IOException, InterruptedException{
		AnalyzeAndroidApps a = new AnalyzeAndroidApps();
		FindBugs2 engine = a.setUpEngine(jarfile.toString(),"FindIntentConstructors",false);
		
		BugCounterObserver obs = new BugCounterObserver();
		engine.getBugReporter().addObserver(obs);
		engine.execute();
		return obs.getBugCount();		
	}

	public static String findIntents(File jarfile) throws PluginException, IOException, InterruptedException{
		AnalyzeAndroidApps a = new AnalyzeAndroidApps();
		FindBugs2 engine = a.setUpEngine(jarfile.toString(),"FindIntentsViaCFG",true);
		
		IntentStringObserver obs = new IntentStringObserver();
		engine.getBugReporter().addObserver(obs);
		engine.execute();
		
		return obs.getIntent().replaceAll(",]", "]").replaceAll(",}", "}");
	}

    private FindBugs2 setUpEngine(String fileUriToJar,String detectorName,boolean useAndroidSdk) throws MalformedURLException, PluginException {
        FindBugs2 engine = new FindBugs2();
        Project project = new Project();
        project.setProjectName("analyze apps");
        engine.setProject(project);

        DetectorFactoryCollection detectorFactoryCollection = DetectorFactoryCollection.instance();
        engine.setDetectorFactoryCollection(detectorFactoryCollection);

        BugCollectionBugReporter bugReporter = new BugCollectionBugReporter(project);
        bugReporter.setPriorityThreshold(Priorities.LOW_PRIORITY);
        bugReporter.setErrorVerbosity(BugReporter.SILENT);
        
        engine.setBugReporter(bugReporter);
        UserPreferences preferences = UserPreferences.createDefaultUserPreferences();
        preferences.setEffort(UserPreferences.EFFORT_MIN);
        
        preferences.enableAllDetectors(false);
		DetectorFactory mydetector = DetectorFactoryCollection.instance().getFactory(detectorName);
		preferences.enableDetector(mydetector, true);
        preferences.getFilterSettings().clearAllCategories();
        
        engine.setUserPreferences(preferences);
        // disable TypeDataflow and ValueNumberDataflow, we don't user their results anyway
        engine.setAnalysisFeatureSettings(preferences.getAnalysisFeatureSettings());
        
        project.addFile(fileUriToJar);
        if(useAndroidSdk)
        	project.addAuxClasspathEntry("android/android.jar");
        fixFindbugMemoryLeak();
        return engine;
    }


    private void fixFindbugMemoryLeak() {
    	fixFindbugMemoryLeak("unconditionalThrowers");
    	fixFindbugMemoryLeak("unsupportedMethods");
    }
	private void fixFindbugMemoryLeak(String fieldName) {
		try {
			Class<MethodInfo> c = MethodInfo.class;
			Field f = c.getDeclaredField(fieldName);
			f.setAccessible(true);
			Method clearMethod = Map.class.getMethod("clear");
			Object map = f.get(null);
			if(map!=null)
				clearMethod.invoke(map);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
}