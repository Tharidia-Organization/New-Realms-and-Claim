# Guida Tecnica per Aggiornamento New-Realms-and-Claim

Questo documento descrive la procedura step-by-step per aggiornare il progetto standalone **New-Realms-and-Claim** partendo dalle modifiche nel progetto principale **Tharidia_items**.

---

## Informazioni Generali

| Progetto | Path | Mod ID | Namespace |
|----------|------|--------|-----------|
| Sorgente | `Tharidia_items` | `tharidiathings` | `tharidiathings` |
| Destinazione | `New-Realms-and-Claim` | `tharidia_realmsandclaim` | `tharidia_realmsandclaim` |

### Regole Fondamentali

1. **Portare SOLO codice relativo a Realms e Claims** - Nient'altro
2. **ESCLUDERE completamente il sistema Dungeon funzionante** - La GUI mostra il tab ma i bottoni sono disabilitati
3. **COPIARE SEMPRE dalla versione ATTUALE** - Mai recuperare vecchie versioni da git
4. **Il progetto DEVE essere identico visivamente e funzionalmente** (eccetto dungeon)

---

## STEP 1: Dipendenze build.gradle

### 1.1 Verificare repository GeckoLib

```groovy
repositories {
    maven {
        name = "Geckolib"
        url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/"
    }
}
```

### 1.2 Aggiungere dipendenza GeckoLib

Copiare la versione esatta da Tharidia_items:

```groovy
dependencies {
    // GeckoLib for animated block rendering
    implementation "software.bernie.geckolib:geckolib-neoforge-1.21.1:4.8.2"

    // ... altre dipendenze
}
```

### 1.3 Dipendenze da NON includere

```
- Dipendenze da altri mod Tharidia (tharidiatweaks, tharidiafeatures)
- flatDir repositories
- copyLatestJars tasks
```

---

## STEP 2: File GeckoLib - Modelli Geo

### 2.1 Copiare il modello GeckoLib

**Sorgente:** `Tharidia_items/src/main/resources/assets/tharidiathings/geo/realm_stage_1.geo.json`
**Destinazione:** `New-Realms-and-Claim/src/main/resources/assets/tharidia_realmsandclaim/geo/realm_stage_1.geo.json`

```bash
# Creare directory se non esiste
mkdir -p src/main/resources/assets/tharidia_realmsandclaim/geo/

# Copiare il file (nessuna modifica necessaria, il geo.json non ha namespace)
cp <sorgente>/geo/realm_stage_1.geo.json <destinazione>/geo/
```

### 2.2 Copiare la texture

**Sorgente:** `Tharidia_items/src/main/resources/assets/tharidiathings/textures/block/realm_stage_1.png`
**Destinazione:** `New-Realms-and-Claim/src/main/resources/assets/tharidia_realmsandclaim/textures/block/realm_stage_1.png`

```bash
cp <sorgente>/textures/block/realm_stage_1.png <destinazione>/textures/block/
```

---

## STEP 3: Modelli JSON Block

### 3.1 Blockstate pietro.json

Il blockstate deve referenziare:
- Livelli 0-1: modelli vuoti (GeckoLib fa il rendering)
- Livelli 2-4: modelli JSON standard

**File:** `blockstates/pietro.json`

```json
{
  "variants": {
    "half=lower,level=0": { "model": "tharidia_realmsandclaim:block/realm_pietro_1" },
    "half=lower,level=1": { "model": "tharidia_realmsandclaim:block/realm_pietro_1" },
    "half=lower,level=2": { "model": "tharidia_realmsandclaim:block/pietro" },
    "half=lower,level=3": { "model": "tharidia_realmsandclaim:block/pietro" },
    "half=lower,level=4": { "model": "tharidia_realmsandclaim:block/pietro" },
    "half=upper,level=0": { "model": "tharidia_realmsandclaim:block/realm_pietro_1_upper" },
    "half=upper,level=1": { "model": "tharidia_realmsandclaim:block/realm_pietro_1_upper" },
    "half=upper,level=2": { "model": "tharidia_realmsandclaim:block/pietro_upper" },
    "half=upper,level=3": { "model": "tharidia_realmsandclaim:block/pietro_upper" },
    "half=upper,level=4": { "model": "tharidia_realmsandclaim:block/pietro_upper" }
  }
}
```

### 3.2 Modelli vuoti per GeckoLib (livelli 0-1)

**File:** `models/block/realm_pietro_1.json`
```json
{
    "textures": {
        "particle": "tharidia_realmsandclaim:block/realm_stage_1"
    },
    "elements": []
}
```

**File:** `models/block/realm_pietro_1_upper.json`
```json
{
    "textures": {
        "particle": "tharidia_realmsandclaim:block/realm_stage_1"
    },
    "elements": []
}
```

### 3.3 Modelli JSON per livelli 2-4

Copiare da sorgente e aggiornare namespace nelle texture:

**File:** `models/block/pietro.json`
- Copiare da sorgente
- Sostituire `tharidiathings:block/pietro` con `tharidia_realmsandclaim:block/pietro`

**File:** `models/block/pietro_upper.json`
- Copiare da sorgente
- Sostituire namespace

### 3.4 Modello Item (GeckoLib)

**File:** `models/item/pietro.json`
```json
{
  "parent": "minecraft:builtin/entity"
}
```

**IMPORTANTE:** `builtin/entity` indica a Minecraft di usare un renderer custom (GeckoLib).

---

## STEP 4: Classi Java GeckoLib - Block Rendering

### 4.1 PietroBlockModel.java

**Path:** `client/model/PietroBlockModel.java`

```java
package com.THproject.tharidia_realmsandclaim.client.model;

import com.THproject.tharidia_realmsandclaim.TharidiaRealmsAndClaim;
import com.THproject.tharidia_realmsandclaim.block.entity.PietroBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PietroBlockModel extends GeoModel<PietroBlockEntity> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        TharidiaRealmsAndClaim.MODID, "geo/realm_stage_1.geo.json"
    );

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        TharidiaRealmsAndClaim.MODID, "textures/block/realm_stage_1.png"
    );

    @Override
    public ResourceLocation getModelResource(PietroBlockEntity blockEntity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PietroBlockEntity blockEntity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PietroBlockEntity blockEntity) {
        return null; // Static model
    }
}
```

### 4.2 PietroBlockRenderer.java

**Path:** `client/renderer/PietroBlockRenderer.java`

Copiare da sorgente e sostituire:
- `com.THproject.tharidia_things` → `com.THproject.tharidia_realmsandclaim`
- `TharidiaThings` → `TharidiaRealmsAndClaim`

Il renderer è ibrido:
- Livelli 0-1: rendering GeckoLib
- Livelli 2-4: return (lascia fare a Minecraft con JSON)

### 4.3 PietroBlockEntity.java - Implementare GeoBlockEntity

Aggiungere import:
```java
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
```

Modificare dichiarazione classe:
```java
public class PietroBlockEntity extends BlockEntity implements MenuProvider, GeoBlockEntity {
```

Aggiungere campo:
```java
private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
```

Aggiungere metodi (alla fine della classe):
```java
@Override
public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    // Static model - no animations
}

@Override
public AnimatableInstanceCache getAnimatableInstanceCache() {
    return geoCache;
}
```

---

## STEP 5: Classi Java GeckoLib - Item Rendering

### 5.1 PietroBlockItem.java

**Path:** `item/PietroBlockItem.java`

```java
package com.THproject.tharidia_realmsandclaim.item;

import com.THproject.tharidia_realmsandclaim.client.renderer.PietroItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class PietroBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PietroBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private PietroItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new PietroItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Static model
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
```

### 5.2 PietroItemModel.java

**Path:** `client/model/PietroItemModel.java`

```java
package com.THproject.tharidia_realmsandclaim.client.model;

import com.THproject.tharidia_realmsandclaim.TharidiaRealmsAndClaim;
import com.THproject.tharidia_realmsandclaim.item.PietroBlockItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PietroItemModel extends GeoModel<PietroBlockItem> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        TharidiaRealmsAndClaim.MODID, "geo/realm_stage_1.geo.json"
    );

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        TharidiaRealmsAndClaim.MODID, "textures/block/realm_stage_1.png"
    );

    @Override
    public ResourceLocation getModelResource(PietroBlockItem item) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PietroBlockItem item) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PietroBlockItem item) {
        return null;
    }
}
```

### 5.3 PietroItemRenderer.java

**Path:** `client/renderer/PietroItemRenderer.java`

```java
package com.THproject.tharidia_realmsandclaim.client.renderer;

import com.THproject.tharidia_realmsandclaim.client.model.PietroItemModel;
import com.THproject.tharidia_realmsandclaim.item.PietroBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PietroItemRenderer extends GeoItemRenderer<PietroBlockItem> {

    private static final float SCALE = 0.25f;

    public PietroItemRenderer() {
        super(new PietroItemModel());
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack,
                                    PietroBlockItem animatable, BakedGeoModel model, boolean isReRender,
                                    float partialTick, int packedLight, int packedOverlay) {
        poseStack.translate(0.4, 0.0, 0.0);
        super.scaleModelForRender(SCALE, SCALE, poseStack, animatable, model, isReRender,
                partialTick, packedLight, packedOverlay);
    }
}
```

---

## STEP 6: Registrazione Item

### 6.1 Aggiornare TharidiaRealmsAndClaim.java

Aggiungere import:
```java
import com.THproject.tharidia_realmsandclaim.item.PietroBlockItem;
```

Modificare registrazione item:
```java
// DA:
public static final DeferredItem<BlockItem> PIETRO_ITEM = ITEMS.registerSimpleBlockItem("pietro", PIETRO);

// A:
public static final DeferredItem<PietroBlockItem> PIETRO_ITEM = ITEMS.register("pietro",
        () -> new PietroBlockItem(PIETRO.get(), new Item.Properties()));
```

---

## STEP 7: KeyBindings e Event Handlers

### 7.1 KeyBindings.java

Deve usare bus **MOD**:
```java
@EventBusSubscriber(modid = "tharidia_realmsandclaim", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    // ...
}
```

### 7.2 ClientKeyHandler.java

Deve usare bus **GAME**:
```java
@EventBusSubscriber(modid = "tharidia_realmsandclaim", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientKeyHandler {
    // ...
}
```

**IMPORTANTE:** Il modid deve essere lowercase: `tharidia_realmsandclaim`

---

## STEP 8: GUI e Componenti

### 8.1 Texture GUI

Copiare tutte le texture da:
`Tharidia_items/src/main/resources/assets/tharidiathings/textures/gui/`

A:
`New-Realms-and-Claim/src/main/resources/assets/tharidia_realmsandclaim/textures/gui/`

File necessari:
- `realm_background.png`
- `bar.png`
- `expansion_button.png` / `expansion_button_pressed.png`
- `claims_button.png` / `claims_button_pressed.png`
- `dungeon_button.png` / `dungeon_button_pressed.png`
- `enter_large_button.png`
- `slot_empty.png` / `slot_selected.png`

### 8.2 Componenti GUI

Copiare e aggiornare namespace:
- `ImageTabButton.java`
- `ImageProgressBar.java`
- `MedievalGuiRenderer.java`

### 8.3 PietroScreen.java - Dungeon Disabilitato

Il tab Dungeon deve essere visibile ma non funzionante:

```java
// Il bottone "Enter" non fa nulla:
enterDungeonButton = ImageTabButton.builder(...)
        .onPress(button -> {
            // NON FARE NULLA - Dungeon disabilitato
        })
        .build();
```

**RIMUOVERE** tutti gli import dungeon:
- `JoinGroupQueuePacket`
- `LeaveGroupQueuePacket`
- `StartGroupDungeonPacket`
- `ClientGroupQueueHandler`
- `DungeonQueryInstance`

---

## STEP 9: Font

### 9.1 File font

**File:** `font/medieval.json`
```json
{
  "providers": [
    {
      "type": "ttf",
      "file": "tharidia_realmsandclaim:crimson_text.ttf",
      "size": 12.0,
      "oversample": 8.0,
      "shift": [0.0, 0.0]
    },
    {
      "type": "reference",
      "id": "minecraft:default"
    }
  ]
}
```

### 9.2 Font TTF

Copiare `crimson_text.ttf` nella directory `font/`

---

## STEP 10: File da NON Portare

### Package Dungeon (ESCLUDERE COMPLETAMENTE)
```
dungeon_query/              # Intero package
DungeonPortalBlock.java
DungeonPortalBlockEntity.java
DungeonQueuePacket.java
JoinGroupQueuePacket.java
LeaveGroupQueuePacket.java
StartGroupDungeonPacket.java
SyncGroupQueuePacket.java
ClientGroupQueueHandler.java
DungeonManager.java
DungeonInstance.java
```

### Funzionalità Non-Realm/Claim
```
- Sistema peso (Weight)
- Sistema stamina
- Sistema dieta
- Sistema video/musica
- Anvil calde
- Stable
- Qualsiasi altro blocco/item non realm/claim
```

---

## STEP 11: Sostituzione Namespace

Per tutti i file Java copiati, sostituire:

| Da | A |
|----|---|
| `com.THproject.tharidia_things` | `com.THproject.tharidia_realmsandclaim` |
| `TharidiaThings.MODID` | `TharidiaRealmsAndClaim.MODID` |
| `TharidiaThings.` | `TharidiaRealmsAndClaim.` |
| `tharidiathings:` | `tharidia_realmsandclaim:` |

---

## STEP 12: Build e Test

### 12.1 Build
```bash
./gradlew clean build
```

### 12.2 Checklist Test

1. [ ] Il mod si avvia senza crash
2. [ ] Il blocco Pietro si piazza correttamente (entrambe le metà)
3. [ ] Il blocco Pietro renderizza con GeckoLib (livelli 0-1)
4. [ ] Il blocco Pietro renderizza con JSON (livelli 2-4)
5. [ ] L'item Pietro nell'inventario mostra il modello GeckoLib
6. [ ] La GUI del regno si apre (click destro)
7. [ ] I 3 tab sono visibili (Expansion, Claims, Dungeon)
8. [ ] Tab Expansion funziona (deposito potatoes, upgrade)
9. [ ] Tab Claims funziona (lista claims, gerarchia)
10. [ ] Tab Dungeon mostra testo ma bottone Enter non fa nulla
11. [ ] Tasto B togla le boundaries del realm
12. [ ] Il blocco Claim funziona
13. [ ] Font medievale funziona nella GUI

---

## Struttura File Completa

```
New-Realms-and-Claim/src/main/
├── java/com/THproject/tharidia_realmsandclaim/
│   ├── TharidiaRealmsAndClaim.java          # Registrazioni
│   ├── TharidiaRealmsAndClaimClient.java    # Client setup
│   ├── block/
│   │   ├── PietroBlock.java
│   │   ├── ClaimBlock.java
│   │   └── entity/
│   │       ├── PietroBlockEntity.java       # + GeoBlockEntity
│   │       └── ClaimBlockEntity.java
│   ├── item/
│   │   └── PietroBlockItem.java             # GeoItem
│   ├── client/
│   │   ├── ClientPacketHandler.java
│   │   ├── ClientKeyHandler.java            # bus = GAME
│   │   ├── KeyBindings.java                 # bus = MOD
│   │   ├── RealmBoundaryRenderer.java
│   │   ├── ClaimBoundaryRenderer.java
│   │   ├── model/
│   │   │   ├── PietroBlockModel.java        # GeoModel block
│   │   │   └── PietroItemModel.java         # GeoModel item
│   │   ├── renderer/
│   │   │   ├── PietroBlockRenderer.java     # GeoBlockRenderer
│   │   │   └── PietroItemRenderer.java      # GeoItemRenderer
│   │   └── gui/
│   │       ├── PietroScreen.java
│   │       ├── ClaimScreen.java
│   │       ├── components/
│   │       │   ├── ImageTabButton.java
│   │       │   └── ImageProgressBar.java
│   │       └── medieval/
│   │           └── MedievalGuiRenderer.java
│   ├── network/
│   │   └── ...
│   └── realm/
│       └── RealmManager.java
└── resources/assets/tharidia_realmsandclaim/
    ├── blockstates/
    │   ├── pietro.json
    │   └── claim.json
    ├── geo/
    │   └── realm_stage_1.geo.json           # GeckoLib model
    ├── models/
    │   ├── block/
    │   │   ├── realm_pietro_1.json          # Vuoto (GeckoLib)
    │   │   ├── realm_pietro_1_upper.json    # Vuoto (GeckoLib)
    │   │   ├── pietro.json                  # JSON per livelli 2-4
    │   │   ├── pietro_upper.json
    │   │   └── claim.json
    │   └── item/
    │       ├── pietro.json                  # builtin/entity
    │       └── claim.json
    ├── textures/
    │   ├── block/
    │   │   ├── realm_stage_1.png            # Texture GeckoLib
    │   │   ├── pietro.png
    │   │   └── claim.png
    │   └── gui/
    │       └── ...
    ├── font/
    │   ├── medieval.json
    │   └── crimson_text.ttf
    └── lang/
        └── en_us.json
```

---

## Troubleshooting

### Modello GeckoLib non renderizza
- Verificare che `geo/realm_stage_1.geo.json` esista
- Verificare che `textures/block/realm_stage_1.png` esista
- Verificare che PietroBlockEntity implementi GeoBlockEntity
- Verificare che PietroBlockRenderer estenda GeoBlockRenderer

### Item non mostra modello GeckoLib
- Verificare che `models/item/pietro.json` contenga `"parent": "minecraft:builtin/entity"`
- Verificare che PietroBlockItem implementi GeoItem
- Verificare che la registrazione item usi PietroBlockItem, non BlockItem

### Keybinding non funziona
- KeyBindings.java → bus `MOD`
- ClientKeyHandler.java → bus `GAME`
- modid deve essere lowercase: `tharidia_realmsandclaim`

### Font non funziona
- Verificare `font/medieval.json` con namespace corretto
- Verificare che `crimson_text.ttf` esista

---

*Documento aggiornato: 2026-02-02*
