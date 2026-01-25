# Star2D Scripting & Engine Guide

This document is the **Master Reference** for AI Agents and Developers to understand the Star2D engine architecture and generate game scripts.

## 1. Project Directory Structure

- **`app/src/main/java/com/star4droid/star2d/ElementDefs/`**: **Item Definitions**.
    - Contains `ItemDef.java` and subclasses (e.g., `BoxDef`, `CircleDef`).
    - Define properties like `width`, `height`, `friction`, `type` (UI/STATIC/DYNAMIC).
- **`app/src/main/java/com/star4droid/template/Items/`**: **Runtime Implementation**.
    - `StageImp.java`: The core scene manager. Manages `GameStage` (World) and `UiStage` (UI).
    - `PlayerItem.java`: The interface for all game entities.
- **`app/src/main/java/com/star4droid/template/Utils/`**: **Utilities**.
    - `Utils.java`: Helpers for Assets, Files, and Math.
    - `ItemScript.java`: Base class for Entity scripts.
    - `SceneScript.java`: Base class for Scene logic.
- **`assets/files/examples/`**: Example projects (JSON scenes/joints in `scenes/` and `joints/`).

## 2. Engine Logic & Architecture

### The Stage System
Star2D uses two stages:
1.  **`GameStage`**: For world entities. Affected by Camera zoom/pan.
2.  **`UiStage`**: For UI elements (`type="UI"`). Fixed camera.

**Rendering**:
-   Unified loop in `StageImp`.
-   Actors from both stages are sorted by **Effective Z-Index** (from `PlayerItem` properties) and drawn together to allow correct layering.

**Input**:
-   `ZOrderedInputProcessor` handles touches.
-   It checks both stages, determines the "top" actor by Z-index, and routes the event there.
-   **Back Key** is routed to `GameStage`.

## 3. Scripting Guide

### A. ItemScript (`ItemScript.java`)
Attached to specific entities.
*   **Context**: Controls one `PlayerItem`.
*   **Core API**:
    -   `Body body`: Box2D physics body.
    -   `Actor actor`: LibGDX actor (visual).
    -   `StageImp stage`: Game world access.
    -   `findItem(String name)`: Find other items.
    -   `setImage(String path)`: Change texture.

**Lifecycle**:
```java
public void onBodyUpdate() { /* 60 FPS update */ }
public void onCollisionBegin(PlayerItem other) { /* Physics collision */ }
public void onTouchStart(InputEvent event) { /* Input */ }
```

### B. SceneScript (`SceneScript.java`)
Attached to the Scene.
*   **Context**: Global logic.
*   **Core API**:
    -   `getStage()`: Access `StageImp`.
*   **Lifecycle**:
    -   `create()`, `draw()`, `pause()`, `resume()`.

## 4. API Reference

### `StageImp`
-   `findItem(String name)`: Get an item in the scene.
-   `score`: Manage via `preferences` or custom vars.
-   `openScene(String name)`: value from `scenes` folder.

### `PlayerItem`
-   `getBody()`: Box2D Body.
-   `getActor()`: LibGDX Actor.
-   `getProperties()`: `ItemDef` (properties).
-   `destroy()`: Remove from game.

## 5. Examples

### Jump Script (ItemScript)
```java
package com.star4droid.Game.Scripts.main;

import com.star4droid.template.Utils.ItemScript;
import com.star4droid.template.Utils.PlayerItem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class PlayerScript extends ItemScript {
    float jumpForce = 500f;

    @Override
    public void onBodyUpdate() {
        body.setFixedRotation(true);
    }

    @Override
    public void onTouchStart(InputEvent event) {
        // Apply impulse to center of mass
        body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
    }
}
```

### Coin Script (ItemScript)
```java
package com.star4droid.Game.Scripts.main;

import com.star4droid.template.Utils.ItemScript;
import com.star4droid.template.Utils.PlayerItem;

public class CoinScript extends ItemScript {
    @Override
    public void onCollisionBegin(PlayerItem other) {
        if(other.getName().equals("Player")) {
            // Add score logic here
            this.playerItem.destroy();
        }
    }
}
```

## 6. AI Generation Rules
1.  **Package**: `com.star4droid.Game.Scripts.main` (default).
2.  **Imports**: Use `com.badlogic.gdx.*` and `com.star4droid.template.Utils.*`.
3.  **Validation**: Always check for `null` when finding items.
4.  **Math**: Use `com.badlogic.gdx.math.MathUtils`.
