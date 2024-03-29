package jediterminalplugin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import tabuterminal.TabuTerminal;
import tabuterminal.TabuTerminalPlugin_V1;

public class JediTerminalPlugin extends tabuterminal.TabuTerminalPlugin_V1 {
	private static final String CHARSET2 = "charset";
	private static final String CYGWIN3 = "cygwin";
	private static final String ANSI_COLOR_ENABLED = "ansiColorEnabled";
	private static final String COMMAND2 = "command";
	private static final String JEDI_SETTINGS = "jediSettings";
	MenuItem jediTermTabItem = new MenuItem("New JediTermTab");
	MenuItem jediTermSettingsItem = new MenuItem("JediTerm Settings");
	Map<String, Object> jediSettings = new HashMap<>();
	private int tabNumber = 1;

	public JediTerminalPlugin(TabuTerminal mainTerminalWindow) {
		super(mainTerminalWindow);
	}

	public static void main(String[] args) {
		JFrame mainFrame = new JFrame("Jedi Terminal CMD PROMPT");
		String[] cmd = { "cmd.exe" };
		JediTermWidget tp = getWidget(cmd, System.getenv(), 80, 24, true, "C:\\", "UTF-8", false, false);
		mainFrame.add(tp);
		mainFrame.setVisible(true);
	}

	public static JediTermWidget getWidget(String[] command, Map<String, String> environment, int initialRows,
			int initialColumns, boolean windowAnsiColor, String dir, String chsetName, boolean isConsole,
			boolean isCygwin) {
		PtyProcessBuilder procBuilder = new PtyProcessBuilder();
		procBuilder.setCommand(command);
		procBuilder.setEnvironment(environment);
		procBuilder.setInitialRows(initialRows);
		procBuilder.setInitialColumns(initialColumns);
		procBuilder.setWindowsAnsiColorEnabled(windowAnsiColor);
		procBuilder.setDirectory(dir);
		Charset cset = Charset.forName(chsetName);
		procBuilder.setConsole(isConsole);
		procBuilder.setCygwin(isCygwin);
		SettingsProvider settings = new DefaultSettingsProvider();
		JediTermWidget jtw = new JediTermWidget(settings);
		try {
			PtyProcess myProcess = procBuilder.start();
			TtyConnector tc = new PtyProcessTtyConnector(myProcess, cset);
			jtw.setTtyConnector(tc);
		} catch (IOException e) {
			java.util.logging.Logger.getLogger(JediTerminalPlugin.class.getName()).log(Level.SEVERE,
					"IO Exception building PTY process", e);

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
			for (Entry<?, ?> e : ((Map<?, ?>) jso).entrySet()) {
				if (!((e.getKey() instanceof String) && (e.getValue() instanceof Object))) {
					typesafe = false;
					break;
				}
			}
			if (typesafe) {
				@SuppressWarnings("unchecked")
				Map<String, Object> jediSettingsTmp = (Map<String, Object>) jso;
				jediSettings = jediSettingsTmp;
			}
		}
	}

	private void showJediTermSettingsWindow() {
		this.jediSettings = this.getJediSettings();
		Stage dialog = new Stage();
		dialog.setTitle("JediTerm Settings");
		VBox mainBox = new VBox();
		// command UI
		HBox commandBox = new HBox();
		VBox.setVgrow(commandBox, Priority.ALWAYS);
		Text cmdText = new Text("Shell Command");
		HBox.setHgrow(cmdText, Priority.ALWAYS);
		TextField cmdField = new TextField();
		HBox.setHgrow(cmdField, Priority.ALWAYS);
		Object cmdo = jediSettings.get(COMMAND2);
		String cmdVal = "cmd.exe";
		if (cmdo instanceof List) {
			@SuppressWarnings("unchecked")//all lists are objects or arutoboxable to objects
			Object[] cmda = ((List<Object>)cmdo).toArray();
			StringBuilder sb = new StringBuilder();
			for (Object s : cmda) {
				sb.append(s.toString()).append(" ");
			}
			cmdVal = sb.toString().trim();
		}
		cmdField.setText(cmdVal);
		jediSettings.put(COMMAND2, cmdVal.split("\\s"));
		commandBox.getChildren().add(cmdText);
		commandBox.getChildren().add(cmdField);
		mainBox.getChildren().add(commandBox);
		// div by 8 for width, 16 for height for console initial width/height
		// Ansi color config
		HBox ansiColorBox = new HBox();
		VBox.setVgrow(ansiColorBox, Priority.ALWAYS);
		CheckBox ansiColorCheckBox = new CheckBox();
		Text ansiColorText = new Text("Enable Windows ANSI Color");
		HBox.setHgrow(ansiColorCheckBox, Priority.ALWAYS);
		HBox.setHgrow(ansiColorText, Priority.ALWAYS);
		ansiColorBox.getChildren().add(ansiColorText);
		ansiColorBox.getChildren().add(ansiColorCheckBox);
		Boolean ansiBool = true;
		Object ansi = jediSettings.get(ANSI_COLOR_ENABLED);
		if (ansi instanceof Boolean) {
			ansiBool = (Boolean) ansi;
		}
		ansiColorCheckBox.setSelected(ansiBool);
		jediSettings.put(ANSI_COLOR_ENABLED, ansiBool);
		mainBox.getChildren().add(ansiColorBox);
		// cygwin UI
		HBox cygwinBox = new HBox();
		VBox.setVgrow(cygwinBox, Priority.ALWAYS);
		CheckBox cygwinCheckBox = new CheckBox();
		Text cygwinText = new Text("Cygwin Environment (set to FALSE unless you know what you are doing)");
		HBox.setHgrow(cygwinCheckBox, Priority.ALWAYS);
		HBox.setHgrow(cygwinText, Priority.ALWAYS);
		cygwinBox.getChildren().add(cygwinText);
		cygwinBox.getChildren().add(cygwinCheckBox);
		Boolean cygwinBool = false;
		Object cygwin = jediSettings.get(CYGWIN3);
		if (cygwin instanceof Boolean) {
			cygwinBool = (Boolean) cygwin;
		}
		cygwinCheckBox.setSelected(cygwinBool);
		jediSettings.put(CYGWIN3, cygwinBool);
		mainBox.getChildren().add(cygwinBox);
		// characterset
		HBox csetBox = new HBox();
		VBox.setVgrow(csetBox, Priority.ALWAYS);
		Text csetText = new Text("Character Set");
		TextField csetField = new TextField();
		String csetName = "UTF-8";
		Object cso = jediSettings.get(CHARSET2);
		if (cso instanceof String) {
			csetName = cso.toString();
		}
		csetField.setText(csetName);
		HBox.setHgrow(csetText, Priority.ALWAYS);
		HBox.setHgrow(csetField, Priority.ALWAYS);
		jediSettings.put(CHARSET2, csetName);
		csetBox.getChildren().add(csetText);
		csetBox.getChildren().add(csetField);
		mainBox.getChildren().add(csetBox);

		Button applyButton = new Button("Apply");
		VBox.setVgrow(applyButton, Priority.ALWAYS);
		applyButton.setOnAction(evt -> {

			String command = cmdField.getText();
			Boolean ansiColorEnabled = ansiColorCheckBox.isSelected();
			Boolean cygwin2 = cygwinCheckBox.isSelected();
			String charset = csetField.getText();
			String[] foo = command.trim().split("\\s");
			jediSettings.put(COMMAND2, foo);
			jediSettings.put(ANSI_COLOR_ENABLED, ansiColorEnabled);
			jediSettings.put(CYGWIN3, cygwin2);
			jediSettings.put(CHARSET2, charset);
			dialog.close();
		});
		mainBox.getChildren().add(applyButton);
		Scene dialogScene = new Scene(mainBox, 1115, 755);
		dialog.setScene(dialogScene);
		dialog.showAndWait();

	}

	public Map<String, Object> getJediSettings() {
		Object j = this.getTerminalWindow().getSettings().get(JEDI_SETTINGS);
		boolean typesafe = false;
		if (j instanceof Map) {
			typesafe = true;
			for (Entry<?, ?> e : ((Map<?, ?>) j).entrySet()) {
				Object key = e.getKey();
				Object val = e.getValue();
				if (!((key instanceof String) && (val instanceof Object))) {
					typesafe = false;
					break;
				}
			}
		}
		Map<String, Object> jediSettingsIntermediate = new HashMap<>();
		if (typesafe) {
			@SuppressWarnings("unchecked")
			Map<String, Object> intermediate = (Map<String, Object>) j;
			jediSettingsIntermediate = intermediate;
		}
		this.getTerminalWindow().getSettings().put(JEDI_SETTINGS, jediSettings);
		return jediSettingsIntermediate;
	}

	private void addJediTermTab() {
		JediTab newTab = new JediTab(jediSettings, this.getTerminalWindow().getMainWindow());
		newTab.setText("JEDI: " + this.tabNumber++);
		this.getTerminalWindow().getTabPane().getTabs().add(newTab);

	}

	@Override
	public void removePlugin() {
		TabuTerminal mainWindow = this.getTerminalWindow();
		Menu tabMenu = mainWindow.getTabMenu();
		Menu settingsMenu = mainWindow.getSettingsMenu();
		tabMenu.getItems().remove(this.jediTermTabItem);
		settingsMenu.getItems().remove(this.jediTermSettingsItem);

	}
	@Override
	public void saveSettings() {
		this.getTerminalWindow().getSettings().put(JEDI_SETTINGS,jediSettings);
	}

}
