package com.partypeteai;

import com.partypeteai.chat.ChatMessage;
import com.partypeteai.chat.ChatRole;
import com.partypeteai.ui.SafeTextRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

public class PartyPeteAiPanel extends PluginPanel
{
	private static final int MAX_CHARS = 4000;
	private static final int PANEL_WIDTH = 360;
	private static final int BUBBLE_WIDTH = 292;
	private static final String[] LOADING = {
		"Party Pete is checking the drop tables…", "Party Pete is asking the Wise Old Man…",
		"Party Pete is searching the party room…", "Party Pete is consulting the quest guide…"
	};
	private static final String[] STARTERS = {
		"What should I train next?", "How do I get a Fire Cape?", "What gear should I use for Barrows?",
		"What are some good F2P money makers?", "Explain Prayer flicking.", "How do I start Recipe for Disaster?"
	};

	private final PartyPeteAiPlugin plugin;
	private final PartyPeteAiConfig config;
	private final JPanel messages = new JPanel();
	private final JScrollPane scroll;
	private final JTextArea input = new JTextArea(3, 20);
	private final JButton send = new JButton("Send");
	private final JButton stop = new JButton("Stop");
	private final JLabel counter = new JLabel("0 / " + MAX_CHARS);
	private final JLabel status = new JLabel("Configuration required");
	private final JLabel contextNote = new JLabel();
	private final Image avatar;
	private Timer loadingTimer;
	private JPanel loadingBubble;
	private JPanel streamRow;
	private JPanel starterPanel;
	private JTextArea streamText;
	private int loadingIndex;
	private boolean busy;

	public PartyPeteAiPanel(PartyPeteAiPlugin plugin, PartyPeteAiConfig config)
	{
		super(false);
		this.plugin = plugin;
		this.config = config;
		setLayout(new BorderLayout(0, 8));
		setPreferredSize(new Dimension(PANEL_WIDTH, 600));
		setMinimumSize(new Dimension(320, 300));
		setBorder(new EmptyBorder(8, 8, 8, 8));
		avatar = ImageUtil.loadImageResource(getClass(), "/party_pete.png");
		add(buildHeader(), BorderLayout.NORTH);
		messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
		messages.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroll = new JScrollPane(messages);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		add(scroll, BorderLayout.CENTER);
		add(buildInput(), BorderLayout.SOUTH);
		showWelcome();
		refreshConfiguration();
	}

	private JPanel buildHeader()
	{
		JPanel header = new JPanel(new BorderLayout(8, 0));
		header.setBorder(new EmptyBorder(2, 2, 8, 2));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.add(new JLabel(new ImageIcon(avatar.getScaledInstance(46, 46, Image.SCALE_SMOOTH))), BorderLayout.WEST);
		JPanel names = new JPanel(); names.setOpaque(false); names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
		JLabel title = new JLabel("Party Pete"); title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
		JLabel subtitle = new JLabel("Your OSRS Assistant"); subtitle.setForeground(Color.LIGHT_GRAY);
		status.setForeground(new Color(120, 210, 130));
		names.add(title); names.add(subtitle); names.add(status);
		header.add(names, BorderLayout.CENTER);
		return header;
	}

	private JPanel buildInput()
	{
		JPanel outer = new JPanel(new BorderLayout(0, 4));
		contextNote.setForeground(new Color(220, 185, 90));
		contextNote.setFont(contextNote.getFont().deriveFont(10f));
		outer.add(contextNote, BorderLayout.NORTH);
		input.setLineWrap(true); input.setWrapStyleWord(true);
		input.setBorder(new EmptyBorder(6, 6, 6, 6));
		input.addKeyListener(new KeyAdapter()
		{
			@Override public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) { e.consume(); submit(); }
			}
			@Override public void keyReleased(KeyEvent e)
			{
				if (input.getText().length() > MAX_CHARS) input.setText(input.getText().substring(0, MAX_CHARS));
				updateControls();
			}
		});
		JScrollPane inputScroll = new JScrollPane(input);
		inputScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		inputScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		outer.add(inputScroll, BorderLayout.CENTER);
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		JButton clear = new JButton("Clear"); clear.addActionListener(e -> plugin.clearConversation());
		send.addActionListener(e -> submit());
		stop.addActionListener(e -> plugin.cancelRequest());
		stop.setVisible(false);
		counter.setForeground(Color.GRAY);
		controls.add(counter); controls.add(clear); controls.add(stop); controls.add(send);
		outer.add(controls, BorderLayout.SOUTH);
		return outer;
	}

	private void submit()
	{
		String question = input.getText().trim();
		if (!question.isEmpty() && send.isEnabled())
		{
			input.setText("");
			updateControls();
			plugin.submitQuestion(question);
		}
	}

	private void showWelcome()
	{
		messages.removeAll();
		starterPanel = null;
		addAssistant("Welcome to the party, adventurer! I’m Party Pete. Ask me anything about Old School RuneScape: quests, bosses, skilling, equipment, money making, account progression or game mechanics.");
		if (config.showStarterQuestions())
		{
			starterPanel = new JPanel();
			starterPanel.setOpaque(false);
			starterPanel.setLayout(new BoxLayout(starterPanel, BoxLayout.Y_AXIS));
			starterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			for (String starter : STARTERS)
			{
				JButton button = new JButton(starter);
				button.setAlignmentX(Component.LEFT_ALIGNMENT);
				button.setMaximumSize(new Dimension(330, 28));
				button.setPreferredSize(new Dimension(330, 24));
				button.addActionListener(e -> { input.setText(starter); updateControls(); submit(); });
				starterPanel.add(button); starterPanel.add(Box.createVerticalStrut(3));
			}
			messages.add(starterPanel);
		}
		revalidate(); repaint();
	}

	public void addUser(String text)
	{
		hideStarterQuestions();
		addBubble(new ChatMessage(ChatRole.USER, text));
	}
	public void addAssistant(String text) { addBubble(new ChatMessage(ChatRole.ASSISTANT, text)); }
	private void hideStarterQuestions()
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::hideStarterQuestions); return; }
		if (starterPanel != null)
		{
			messages.remove(starterPanel);
			starterPanel = null;
			messages.revalidate();
			messages.repaint();
		}
	}
	public void appendStream(String delta)
	{
		if (delta == null || delta.isEmpty()) return;
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> appendStream(delta)); return; }
		if (streamRow == null)
		{
			if (loadingTimer != null) { loadingTimer.stop(); loadingTimer = null; }
			if (loadingBubble != null) { messages.remove(loadingBubble); loadingBubble = null; }
			streamRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
			streamRow.setOpaque(false);
			streamRow.setAlignmentX(Component.LEFT_ALIGNMENT);
			streamRow.add(new JLabel(new ImageIcon(avatar.getScaledInstance(24, 24, Image.SCALE_SMOOTH))));
			streamText = new JTextArea();
			streamText.setEditable(false); streamText.setLineWrap(true); streamText.setWrapStyleWord(true);
			streamText.setForeground(Color.WHITE); streamText.setBackground(new Color(62, 62, 62));
			streamText.setBorder(new EmptyBorder(7, 7, 7, 7));
			streamText.setPreferredSize(new Dimension(BUBBLE_WIDTH, 70));
			streamRow.add(streamText);
			messages.add(streamRow);
		}
		streamText.append(delta);
		streamText.setSize(new Dimension(BUBBLE_WIDTH, Short.MAX_VALUE));
		int height = Math.max(45, streamText.getPreferredSize().height);
		streamText.setPreferredSize(new Dimension(BUBBLE_WIDTH, height));
		messages.revalidate(); messages.repaint();
		scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
	}

	public void finishStream(String finalText)
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> finishStream(finalText)); return; }
		if (streamRow != null)
		{
			messages.remove(streamRow);
			streamRow = null;
			streamText = null;
		}
		addAssistant(finalText);
	}
	private void addBubble(ChatMessage message)
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> addBubble(message)); return; }
		JPanel row = new JPanel(new FlowLayout(message.getRole() == ChatRole.USER ? FlowLayout.RIGHT : FlowLayout.LEFT, 4, 2));
		row.setOpaque(false); row.setAlignmentX(Component.LEFT_ALIGNMENT);
		Color color = message.getRole() == ChatRole.USER ? new Color(52, 83, 112) : new Color(62, 62, 62);
		if (message.getRole() == ChatRole.ASSISTANT)
			row.add(new JLabel(new ImageIcon(avatar.getScaledInstance(24, 24, Image.SCALE_SMOOTH))));
		javax.swing.JEditorPane text = SafeTextRenderer.create(message.getContent(), config.allowSafeLinks(), color);
		text.setSize(new Dimension(BUBBLE_WIDTH, Short.MAX_VALUE));
		int bubbleHeight = Math.max(38, text.getPreferredSize().height);
		text.setPreferredSize(new Dimension(BUBBLE_WIDTH, bubbleHeight));
		text.setBorder(BorderFactory.createLineBorder(color.brighter()));
		row.add(text);
		messages.add(row); messages.add(Box.createVerticalStrut(4));
		messages.revalidate(); messages.repaint();
		SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum()));
	}

	public void setBusy(boolean value)
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(() -> setBusy(value)); return; }
		busy = value;
		stop.setVisible(value);
		if (value)
		{
			status.setText("Thinking");
			loadingBubble = new JPanel(new BorderLayout());
			loadingBubble.setOpaque(false);
			JLabel label = new JLabel(LOADING[0]); label.setForeground(Color.LIGHT_GRAY); loadingBubble.add(label);
			loadingBubble.setAlignmentX(Component.LEFT_ALIGNMENT); messages.add(loadingBubble);
			loadingTimer = new Timer(1800, e -> { loadingIndex = (loadingIndex + 1) % LOADING.length; label.setText(LOADING[loadingIndex]); });
			loadingTimer.start();
		}
		else
		{
			if (loadingTimer != null) { loadingTimer.stop(); loadingTimer = null; }
			if (loadingBubble != null) { messages.remove(loadingBubble); loadingBubble = null; messages.revalidate(); messages.repaint(); }
			refreshConfiguration();
		}
		updateControls();
	}

	public void refreshConfiguration()
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::refreshConfiguration); return; }
		boolean ready = config.apiKey() != null && !config.apiKey().trim().isEmpty();
		status.setText(ready ? "Ready · " + config.provider() : "Configuration required");
		contextNote.setText(config.includeAccountContext() ? "Account context enabled: name, skills, total, login state and region may be sent." : "");
		if (config.wikiGrounding() || config.liveGePrices())
		{
			String data = config.wikiGrounding() && config.liveGePrices() ? "Wiki + live GE enabled." : config.wikiGrounding() ? "Live Wiki grounding enabled." : "Live GE prices enabled.";
			contextNote.setText((contextNote.getText().isEmpty() ? "" : contextNote.getText() + " ") + data);
		}
		updateControls();
	}

	public void showProviderError()
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::showProviderError); return; }
		status.setText("Provider error");
		status.setForeground(new Color(230, 110, 100));
	}

	private void updateControls()
	{
		int count = input.getText().length();
		counter.setText(count + " / " + MAX_CHARS);
		boolean configured = config.apiKey() != null && !config.apiKey().trim().isEmpty();
		send.setEnabled(configured && !busy && count > 0);
		input.setEnabled(!busy);
	}

	public void clear()
	{
		if (!SwingUtilities.isEventDispatchThread()) { SwingUtilities.invokeLater(this::clear); return; }
		setBusy(false); showWelcome();
	}

	public void shutdown()
	{
		if (loadingTimer != null) loadingTimer.stop();
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension preferred = super.getPreferredSize();
		return new Dimension(PANEL_WIDTH, preferred.height);
	}

	@Override
	public Dimension getMinimumSize()
	{
		Dimension minimum = super.getMinimumSize();
		return new Dimension(320, minimum.height);
	}
}
