# https://discord.gg/dqnbKqkFtM

# ZorvynEventItems
A Paper 26.1.2 plugin by BasperLasper that lets server staff create event kits from their inventory, allow players to claim them, and cleanly remove all kit items from every player when the event ends.

## Features
- Create kits directly from what's in your inventory
- Players can claim kits once each
- Ending a kit removes all its items from every online player instantly
- Items are tagged with invisible data + lore so they're always trackable
- Scans inventory, ender chest, and any opened chest for expired items
- Kits persist across server restarts (saved to kits.yml)
- All messages fully configurable in config.yml with & color codes

## Commands
| Command | Permission | Description |
|---|---|---|
| `/eventitem create <id>` | `zorvyneventitems.create` | Create a kit from your current inventory |
| `/eventitem claim <id>` | `zorvyneventitems.claim` | Claim a kit (once per player) |
| `/eventitem end <id>` | `zorvyneventitems.end` | End a kit and remove all its items from every player |
| `/eventitem list` | none | List all active kits |

Alias: `/ei`

## Permissions
| Permission | Default | Description |
|---|---|---|
| `zorvyneventitems.create` | OP | Create event kits |
| `zorvyneventitems.end` | OP | End event kits |
| `zorvyneventitems.claim` | Everyone | Claim event kits |

## Configuration
All messages are editable in `config.yml`. Supports `&` color codes and the following placeholders:

| Placeholder | Where | Meaning |
|---|---|---|
| `{id}` | Most messages | The kit ID |
| `{count}` | create, end, removed | Number of items |
| `{kits}` | list | Comma-separated list of active kits |

## Installation
1. Drop `ZorvynEventItems.jar` into your server's `plugins/` folder
2. Restart the server
3. Edit `plugins/ZorvynEventItems/config.yml` to customise messages
4. Kits are saved automatically to `plugins/ZorvynEventItems/kits.yml`

## Requirements
- Paper 26.1.2
- Java 25
