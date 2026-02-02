# Tharidia - Realms and Claim

> **⚠️ ALPHA VERSION - UNDER ACTIVE DEVELOPMENT**  
> This mod is in early alpha stage and may contain bugs or incomplete features.

## About This Mod

**Tharidia - Realms and Claim** is a specialized mod derived from the main **Tharidia Things** project. It has been extracted and developed as a standalone module to provide advanced territory management features for the Tharidia server.

### What is Tharidia?

Tharidia is an active and complex medieval roleplay Minecraft server with fantasy elements. It features intricate custom mechanics that significantly differentiate the gameplay from vanilla Minecraft, creating a unique roleplay experience with deep territorial and political systems.

This mod is part of the **Tharidia Project** family - a collection of interconnected mods that work together to power the Tharidia server experience.

## Features

### 🏰 Realm System
- **Pietro Blocks**: Special realm markers that define territorial boundaries
- **Realm Visualization**: Real-time 3D boundary rendering with customizable colors
- **Realm Hierarchy**: Multi-tier permission system with ranks (Owner, Noble, Citizen, Colonist)
- **Dynamic Realm Management**: Create, expand, and manage territorial realms
- **Realm Overlay**: In-game HUD showing current realm information

### 🛡️ Claim System
- **Claim Blocks**: Player-owned protected areas within realms
- **Claim Protection**: Comprehensive protection against griefing and unauthorized access
- **Claim Visualization**: Visual boundaries for claimed territories
- **Claim Expiration**: Automatic claim management based on player activity
- **Claim Registry**: Persistent storage and synchronization of all claims

### 👥 Hierarchy & Permissions
- **Rank-based Permissions**: Different access levels for realm members
  - **Owner**: Full control over the realm
  - **Noble**: Administrative privileges
  - **Citizen**: Standard member access
  - **Colonist**: Basic access rights
- **Dynamic Permission Updates**: Real-time synchronization across all players
- **Hierarchy Management GUI**: User-friendly interface for managing realm members

### 🔒 Protection Mechanics
- **Block Protection**: Prevents unauthorized block breaking and placement
- **Crop Protection**: Configurable protection for farmland and crops
- **Entity Protection**: Safeguards against unauthorized entity interactions
- **Explosion Protection**: Prevents damage from explosions in protected areas
- **Fire Protection**: Prevents fire spread in claimed territories

### 🎮 User Interface
- **Claim Management Screen**: Intuitive GUI for managing claims
- **Pietro Management Screen**: Interface for realm configuration
- **Visual Boundaries**: Real-time rendering of realm and claim borders
- **Keybindings**: Customizable controls for quick access to features

### 🌐 Multiplayer Features
- **Network Synchronization**: Efficient client-server data sync
- **Database Integration**: MySQL support for persistent data storage
- **Multi-player Support**: Designed for large-scale multiplayer servers
- **Performance Optimized**: Efficient rendering and data management

## Installation

### Requirements
- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.215 or higher
- **Java**: 21

### Steps
1. Download and install NeoForge for Minecraft 1.21.1
2. Download the latest release of Tharidia - Realms and Claim
3. Place the JAR file in your `mods` folder
4. Launch Minecraft with the NeoForge profile

### Dependencies
This mod requires:
- **Tharidia Tweaks** (companion mod)
- **Tharidia Features** (companion mod)

These dependencies are automatically included in the development environment but must be installed separately for production use.

## Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd New-Realms-and-Claim

# Build the mod
./gradlew build

# The compiled JAR will be in build/libs/
```

## Configuration

The mod includes several configuration options accessible through:
- NeoForge mod configuration menu
- In-game configuration screens
- Server-side config files

## Commands

### Player Commands
- `/claim` - Manage your claims
- `/realm` - View realm information

### Admin Commands
- `/claimadmin` - Administrative claim management
- `/realmadmin` - Administrative realm management

## Technical Information

### Package Structure
- `com.THproject.tharidia_realmsandclaim.realm` - Realm management system
- `com.THproject.tharidia_realmsandclaim.claim` - Claim system
- `com.THproject.tharidia_realmsandclaim.block` - Custom blocks (Pietro, Claim)
- `com.THproject.tharidia_realmsandclaim.block.entity` - Block entities
- `com.THproject.tharidia_realmsandclaim.client` - Client-side rendering and UI
- `com.THproject.tharidia_realmsandclaim.network` - Network packet handling
- `com.THproject.tharidia_realmsandclaim.event` - Event handlers
- `com.THproject.tharidia_realmsandclaim.gui` - GUI menus

### Database
The mod uses MySQL for persistent storage of:
- Realm data and boundaries
- Claim ownership and permissions
- Player hierarchies and ranks
- Claim expiration tracking

## Development Status

This mod is currently in **alpha development**. Features are being actively developed and tested. Expect:
- Potential bugs and issues
- Breaking changes between versions
- Incomplete features
- Active development and updates

## Credits

**Development Team**: Frenk02, Tharidia Development Team  
**Original Project**: Tharidia Things  
**Server**: Chronicles: Tharidia

## License

GNU v3

---

*Part of the Tharidia Project - A collection of mods for medieval roleplay gameplay*
