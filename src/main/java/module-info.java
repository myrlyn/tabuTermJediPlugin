module jediterminalplugin{
	exports jediterminalplugin;
	requires transitive pty4j;
	requires transitive  jediterm.pty;
	requires transitive  java.logging;
	requires transitive  TabuTerminal;
	requires  transitive java.desktop;
	requires  transitive javafx.swing;
	requires  transitive javafx.graphics;
	//requires transitive tabuterminal; 
}