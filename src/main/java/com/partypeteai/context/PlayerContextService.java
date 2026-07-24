package com.partypeteai.context;

import java.util.concurrent.CompletableFuture;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;

public class PlayerContextService
{
	private final Client client;
	private final ClientThread clientThread;
	public PlayerContextService(Client client, ClientThread clientThread) { this.client = client; this.clientThread = clientThread; }

	public CompletableFuture<String> capture(boolean enabled)
	{
		if (!enabled) return CompletableFuture.completedFuture("");
		CompletableFuture<String> future = new CompletableFuture<>();
		clientThread.invoke(() ->
		{
			try { future.complete(build()); }
			catch (RuntimeException e) { future.complete(""); }
		});
		return future;
	}

	private String build()
	{
		boolean loggedIn = client.getGameState() == GameState.LOGGED_IN;
		StringBuilder out = new StringBuilder("Logged in: ").append(loggedIn);
		if (!loggedIn) return out.toString();
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			out.append("\nDisplay name: ").append(player.getName());
			WorldPoint point = player.getWorldLocation();
			if (point != null) out.append("\nMap region ID: ").append(point.getRegionID());
		}
		out.append("\nTotal level: ").append(client.getTotalLevel()).append("\nSkills:");
		for (Skill skill : Skill.values())
		{
			if (skill == Skill.OVERALL) continue;
			out.append("\n- ").append(skill.getName()).append(": base ")
				.append(client.getRealSkillLevel(skill)).append(", boosted ")
				.append(client.getBoostedSkillLevel(skill));
		}
		return out.toString();
	}
}

