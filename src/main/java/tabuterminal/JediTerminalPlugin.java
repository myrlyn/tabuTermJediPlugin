package tabuterminal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.swing.JFrame;

import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class JediTerminalPlugin extends tabuterminal.TabuTerminalPlugin_V1{
	private static final String JEDI_SETTINGS = "jediSettings";
	MenuItem jediTermTabItem = new MenuItem("New JediTermTab");
	MenuItem jediTermSettingsItem = new MenuItem("JediTerm Settings");
	Map <String,Object> jediSettings = new HashMap<>();
	public JediTerminalPlugin(TabuTerminal mainTerminalWindow) {
		super(mainTerminalWindow);
	}

	public static void main(String[] args) {
		JFrame mainFrame = new JFrame("Jedi Terminal CMD PROMPT");
		String[] cmd = {"cmd.exe"};
		JediTermWidget tp = getWidget(cmd,System.getenv(),80,24,true,"C:\\","UTF-8",false,false);
		mainFrame.add(tp);
		mainFrame.setVisible(true);
	}
	public static JediTermWidget 	(String[] command, Map<String, String> environment, int initialRows, int initialColumns, boolean windowAnsiColor, 
			String dir, String chsetName, boolean isConsole, boolean isCygwin) {
		PtyProcessBuilder procBuilder = new PtyProcessBuilder();
		procBuilder.setCommand(command);
		procBuilder.setEnvironment(environment);
		procBuilder.setInitialRows(initialRows);
		procBuilder.setInitialColumns(initialColumns);
		procBuilder.setWindowsAnsiColorEnabled(windowAnsiColor);
		procBuilder.setDirectory(dir);
		Charset cset= Charset.forName(chsetName);
		procBuilder.setConsole(isConsole);
		procBuilder.setCygwin(isCygwin);
		SettingsProvider settings = new DefaultSettingsProvider();
		JediTermWidget jtw = new JediTermWidget(settings);
		try {
			PtyProcess myProcess = procBuilder.start();
			TtyConnector tc = new PtyProcessTtyConnector(myProcess,cset);
			jtw.setTtyConnector(tc);
		} catch (IOException e) {
			java.util.logging.Logger.getLogger(JediTerminalPlugin.class.getName()).log(Level.SEVERE,"IO Exception building PTY process",e);
			
		}
		jtw.start();
		return jtw;
	}

	@Override
	public String getPluginName() {
		
		return "jediTerm";
	}

	@Override
	public void initialize(String jf) {
		TabuTerminal mainWindow = this.getTerminalWindow();
		Menu tabMenu = mainWindow.getTabMenu();
		Menu settingsMenu = mainWindow.getSettingsMenu();
		Map<String, Object> settings = mainWindow.getSettings();
		jediTermTabItem.setOnAction(evt -> addJediTermTab());
		jediTermSettingsItem.setOnAction(evt -> showJediTermSettingsWindow());
		tabMenu.getItems().add(jediTermTabItem);
		settingsMenu.getItems().add(jediTermSettingsItem);
		Object jso = settings.get(JEDI_SETTINGS);
		if (jso instanceof Map) {
			boolean typesafe = true;
			for (Entry<?,?> e: ((Map<?,?>)jso).entrySet()) {
				if (!((e.getKey() instanceof String ) &&(e.getValue() instanceof Object))) {
					typesafe=false;
					break;
				}
			}
			if (typesafe) {
				@SuppressWarnings("unchecked")
				Map<String,Object> jediSettingsTmp = (Map<String,Object>)jso;
				jediSettings=jediSettingsTmp;
			}
		}
	}

	

	private void showJediTermSettingsWindow() {
		
		
		Stage dialog = new Stage();
		dialog.setTitle("JediTerm Settings");
		VBox mainBox = new VBox();
		//command UI
		HBox commandBox = new HBox();
		VBox.setVgrow(commandBox, Priority.ALWAYS);
		Text cmdText = new Text("Shell Command");
		HBox.setHgrow(cmdText, Priority.ALWAYS);
		TextField cmdField = new TextField();
		HBox.setHgrow(cmdField, Priority.ALWAYS);
		Object cmdo = jediSettings.get("command");
		String cmdVal ="cmd.exe";
		if (cmdo instanceof String[]) {
			String[] cmda = (String[])cmdo;
			StringBuilder sb = new StringBuilder();
			for (String s: cmda) {
				sb.append(s).append(" ");
			}
			cmdVal = sb.toString().trim();
		}
		cmdField.setText(cmdVal);
		jediSettings.put("command",cmdVal.split("\\s"));
		commandBox.getChildren().add(cmdText);
		commandBox.getChildren().add(cmdField);
		mainBox.getChildren().add(commandBox);
		// div by 8 for width, 16 for height for console initial width/height
		//Ansi color config
		HBox ansiColorBox = new HBox();
		VBox.setVgrow(ansiColorBox, Priority.ALWAYS);
		CheckBox ansiColorCheckBox = new CheckBox();
		Text ansiColorText = new Text("Enable Windows ANSI Color");
		HBox.setHgrow(ansiColorCheckBox, Priority.ALWAYS);
		HBox.setHgrow(ansiColorText, Priority.ALWAYS);
		ansiColorBox.getChildren().add(ansiColorText);
		ansiColorBox.getChildren().add(ansiColorCheckBox);
		Boolean ansiBool = true;
		Object ansi = jediSettings.get("ansiColorEnabled");
		if (ansi instanceof Boolean) {
			ansiBool = (Boolean)ansi;
		}
		ansiColorCheckBox.setSelected(ansiBool);
		jediSettings.put("ansiColorEnabled",ansiBool);
		mainBox.getChildren().add(ansiColorBox);
		//cygwin UI
		HBox cygwinBox = new HBox();
		VBox.setVgrow(cygwinBox, Priority.ALWAYS);
		CheckBox cygwinCheckBox = new CheckBox();
		Text cygwinText = new Text("Cygwin Environment (set to FALSE unless you know what you are doing)");
		HBox.setHgrow(cygwinCheckBox, Priority.ALWAYS);
		HBox.setHgrow(cygwinText, Priority.ALWAYS);
		cygwinBox.getChildren().add(cygwinText);
		cygwinBox.getChildren().add(cygwinCheckBox);
		Boolean cygwinBool = false;
		Object cygwin = jediSettings.get("cygwin");
		if (cygwin instanceof Boolean) {
			cygwinBool = (Boolean)cygwin;
		}
		cygwinCheckBox.setSelected(cygwinBool);
		jediSettings.put("cygwin",cygwinBool);
		mainBox.getChildren().add(cygwinBox);
		//characterset 
		HBox csetBox = new HBox();
		VBox.setVgrow(csetBox, Priority.ALWAYS);
		Text csetText = new Text("Character Set");
		TextField csetField = new TextField();
		String csetName = "UTF-8";
		Object cso = jediSettings.get("charset");
		if (cso instanceof String) {
			csetName = cso.toString();
		}
		csetField.setText(csetName);
		HBox.setHgrow(csetText, Priority.ALWAYS);
		HBox.setHgrow(csetField, Priority.ALWAYS);
		jediSettings.put("charset",csetName);
		csetBox.getChildren().add(csetText);
		csetBox.getChildren().add(csetField);
		mainBox.getChildren().add(csetBox);
		
		Button applyButton = new Button("Apply");
		VBox.setVgrow(applyButton, Priority.ALWAYS);
		applyButton.setOnAction(evt -> {
			//TODO implement apply settings
		});
		mainBox.getChildren().add(applyButton);
		Scene  dialogScene= new Scene(mainBox,1115, 755);
		dialog.setScene(dialogScene);
		dialog.showAndWait();
		
	}

	private Map<String, Object> getJediSettings() {
		Object j = this.getTerminalWindow().getSettings().get(JEDI_SETTINGS);
		boolean typesafe = false;
		if (j instanceof Map) {
			typesafe=true;
			for (Entry<?,?> e: ((Map<?,?>)j).entrySet()) {
				Object key = e.getKey();
				Object val = e.getValue();
				if (!((key instanceof String )&&(val instanceof Object))) {
					typesafe=false;
					break;
				}
			}
		}
		Map<String, Object> jediSettings_int = new HashMap<>();
		if (typesafe) {
			@SuppressWarnings("unchecked")
			Map<String, Object> intermediate = (Map<String,Object>)j;
			jediSettings_int = intermediate;
		}
		this.getTerminalWindow().getSettings().put(JEDI_SETTINGS,jediSettings);
		return jediSettings_int;
	}

	private void addJediTermTab() {
		// TODO Auto-generated method stub
	}

	@Override
	public void removePlugin() {
		TabuTerminal mainWindow = this.getTerminalWindow();
		Menu tabMenu = mainWindow.getTabMenu();
		Menu settingsMenu = mainWindow.getSettingsMenu();
		tabMenu.getItems().remove(this.jediTermTabItem);
		settingsMenu.getItems().remove(this.jediTermSettingsItem);
		
	}

}
