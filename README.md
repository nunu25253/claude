# claude

Claude Code project config. Opening this repo in Claude Code (including Claude Code on the web) auto-provisions:

## UI/UX Pro Max skill

[nextlevelbuilder/ui-ux-pro-max-skill](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) — a design-intelligence skill with a searchable database of 50+ UI styles, 97 color palettes, and 57 font pairings (plus product types, UX guidelines, and chart types) across 10+ tech stacks.

Wired up via:
- `.claude/settings.json` — declares the marketplace (`extraKnownMarketplaces`) and enables the plugin (`enabledPlugins`).
- `.claude/hooks/session-start.sh` — a `SessionStart` hook that runs on Claude Code on the web and actively runs `claude plugin marketplace add` + `claude plugin install` so the skill loads without any manual step.

## Magic MCP (21st.dev)

[21st-dev/magic-mcp](https://github.com/21st-dev/magic-mcp) — generates UI components (shadcn/ui + Tailwind) from natural-language prompts via the `/ui` command, plus a component inspiration browser and logo/brand-asset search.

Wired up via:
- `.mcp.json` — declares the `@21st-dev/magic` MCP server (`npx -y @21st-dev/magic@latest`).
- `.claude/settings.json` — `enableAllProjectMcpServers: true` so it's approved automatically instead of prompting.

**Setup required:** get a free API key from [21st.dev/magic/console](https://21st.dev/magic/console) and set it as the `MAGIC_API_KEY` environment variable/secret in your Claude Code environment settings. `.mcp.json` reads it via `${MAGIC_API_KEY}` so the key itself never has to be committed.
