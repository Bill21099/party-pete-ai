# Party Pete AI

Party Pete AI is a read-only RuneLite sidebar assistant for questions about Old School RuneScape. It can explain quests, bosses, raids, skills, equipment, minigames, account progression, money making, lore, and OSRS-related RuneLite features. It never clicks, types into the game, moves the player, or automates gameplay.

<img width="362" height="1019" alt="Screenshot_1" src="https://github.com/user-attachments/assets/94cbbc31-d374-4941-95ef-a82aa5165503" />
<img width="360" height="1012" alt="Screenshot_6" src="https://github.com/user-attachments/assets/039f0703-c9ab-4b3c-8e76-aec8d61bed5d" />
<img width="345" height="400" alt="Screenshot_3" src="https://github.com/user-attachments/assets/e7f63f53-f71d-44a9-831f-cc5d43f18c63" />
<img width="355" height="1019" alt="Screenshot_2" src="https://github.com/user-attachments/assets/1ee2c225-02e5-4a15-ae88-c463911f8289" />

## Features

- A dark RuneLite-native chat panel with Party Pete’s actual OSRS Wiki chathead bundled locally.
- Gemini, OpenAI, DeepSeek, and OpenRouter support.
- Streamed asynchronous answers with cancellation and configurable timeouts.
- Brief, normal, and detailed answer lengths; precise, balanced, and creative modes.
- In-memory conversation history with 2, 6, 10, or uncapped full-session memory.
- Optional live OSRS Wiki grounding and OSRS Wiki Grand Exchange price data.
- Output limits are controlled by the selected preset: Brief 250, Normal 500, or Detailed 750 generated tokens.
- Defensive structured-response parsing and a fixed local refusal for non-OSRS questions.
- A fixed local refusal for bots, macros, ban evasion, phishing, RWT, and other rule-breaking requests.
- Escaped lightweight formatting and optional HTTPS-only links.
- Optional, read-only account context.

## Setup

1. Obtain an API key from the provider you intend to use:
   - [Google AI Studio](https://aistudio.google.com/app/apikey)
   - [OpenAI API keys](https://platform.openai.com/api-keys)
   - [DeepSeek Platform](https://platform.deepseek.com/api_keys)
   - [OpenRouter keys](https://openrouter.ai/settings/keys)
2. Open RuneLite settings, find **Party Pete AI**, and select the provider.
3. Paste the key into the secret API key field.
4. Select **Provider default**, a matching preset, or **Custom model** and enter an exact provider model ID.
5. Toggle **Test provider**. It sends one short OSRS request and immediately resets.
6. Open Party Pete from the sidebar and ask a question.

Providers can charge for API usage. Model availability, pricing, and rate limits are controlled by the provider and the user's account.

## Privacy and security

Requests travel directly from RuneLite to the selected provider's recognised HTTPS API host. There is no developer proxy, analytics, or telemetry. API keys are used only in the selected provider's authentication header and are never logged. RuneLite configuration storage is not claimed to be encrypted.

Conversation history stays in memory and disappears when the plugin/client closes. **Clear** removes it immediately.

Account context is disabled by default. If enabled, Party Pete may send the current display name, login state, base/boosted skill levels, total level, and map region ID. It does not read or send credentials, bank PINs, account tokens, private/public/clan chat, friends, inventory, or equipment. The settings panel and chat input display an opt-in note before this context is used.

The renderer escapes generated content and only activates links with an `https://` URL, a host, and no embedded credentials. Provider endpoints are hardcoded and checked against a host allowlist.

## Scope and fair-play policy

The assistant is only for OSRS and OSRS-focused comparisons with RuneScape 3. Unrelated questions receive a fixed local refusal. It does not provide bots, macros, automated input, detection avoidance, ban evasion, packet manipulation, RWT, scams, phishing, account theft, or exploit instructions.

Answers are generated and can be wrong. Live Wiki grounding and Grand Exchange prices are available as separate opt-in settings. When disabled or when a lookup fails, current prices, recent updates, changing drop rates, and other time-sensitive details should still be checked against the [OSRS Wiki](https://oldschool.runescape.wiki/) or official update notes.


## Known limitations

- One streamed request is active per panel so partial replies cannot interleave or corrupt conversation order.
- Wiki and price lookups are opt-in and gracefully fall back to provider knowledge when unavailable.
- Provider model IDs and APIs can change; presets are centralised in `ModelCatalog`.
- Full-session memory is uncapped and can eventually exceed a provider’s context limit during extremely long sessions.
- The current RuneLite config API has no action-button value type, so **Clear conversation** and **Test provider** appear as momentary toggles and reset automatically. A regular Clear button is also in the chat panel.
- Provider connection tests require a valid user key and may incur a small provider charge.

## Disclaimer

This is an unofficial third-party plugin and is not affiliated with, endorsed by, or sponsored by Jagex, RuneLite, OpenAI, Google, DeepSeek, or OpenRouter. Party Pete and Old School RuneScape are associated with Jagex. Provider names and trademarks belong to their respective owners.

The bundled Party Pete chathead is sourced from the [OSRS Wiki Party Pete page](https://oldschool.runescape.wiki/w/Party_Pete). Wiki content is published under CC BY-NC-SA 3.0 with additional terms; RuneScape imagery and trademarks belong to Jagex.

## Licence

[BSD 2-Clause](LICENSE).
