package jediterminalplugin;

import java.util.HashMap;
import java.util.Map;

import com.jediterm.terminal.ui.JediTermWidget;

import javafx.embed.swing.SwingNode;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

public class JediTab extends Tab {
	public JediTermWidget TerminalWidget = null;
	public SwingNode swingNode = new SwingNode();

	public JediTab(Map<String, Object> jediSettings, Stage mainWindow) {
		super();
		Object commando = jediSettings.get("command");
		String[] cmd = { "cmd.exe" };
		if (commando instanceof String[]) {
			cmd = (String[]) commando;
		}
		Map<String, String> env = System.getenv();
		Map<String, String> env2 = new HashMap<String,String>();
		for (Map.Entry<String, String> e: env.entrySet()) {
			env2.put(e.getKey(), e.getValue());
		}
		env=env2;
		env.put("TERM", "vt100");
		Boolean ansiColor = true;
		Object ansiColoro = jediSettings.get("ansiColorEnabled");
		if (ansiColoro instanceof Boolean) {
			ansiColor = (Boolean) ansiColoro;
		}
		Boolean cygwin = false;
		Object cygwino = jediSettings.get("cygwin");
		if (cygwino instanceof Boolean) {
			cygwin = (Boolean) cygwino;
		}
		int width = ((int) mainWindow.getWidth()) / 8;
		int height = ((int) mainWindow.getHeight()) / 16;
		env.put("LINES", "" + height);
		env.put("COLUMNS", "" + height);
		TerminalWidget = JediTerminalPlugin.getWidget(cmd, env, height, width, ansiColor, ".", "UTF-8", false, cygwin);
		this.setContent(swingNode);
		swingNode.setContent(TerminalWidget);
	}
}
