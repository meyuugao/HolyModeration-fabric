# Task 1 — GUI elements, demo tab, color theme system (both MC versions)

## Summary
- Added 5 new interactive GUI drawable elements + enhanced ColorPicker.
- Added a demo "Elements" tab in the main GUI with a tab bar (General/Elements).
- Added a global color theme system (ThemeService + ThemePalette) that derives a full
  palette from two base colors (GuiConfig.mainColor / secondColor) via HSB gradient sampling.
- Wired existing HUD elements and MainGuiScreen to the theme palette.
- Applied to both branches: 1.21.11 (Matrix3x2fStack render API) and 1.20.1 (MatrixStack render API).

## New files (both branches)
- gui/drawable/element/impl/impl/SliderDrawableElement.java
- gui/drawable/element/impl/impl/ToggleDrawableElement.java
- gui/drawable/element/impl/impl/CheckboxDrawableElement.java
- gui/drawable/element/impl/impl/DropdownDrawableElement.java
- gui/drawable/element/impl/impl/SearchDrawableElement.java
- gui/tabs/impl/main/ElementsTab.java
- util/service/ThemePalette.java
- util/service/ThemeService.java

## Modified files (both branches)
- gui/drawable/element/impl/impl/ColorPickerDrawableElement.java — added selected color + Consumer<Color> + handleClick
- gui/tabs/Tab.java — LinkedHashMap + input forwarding (click/scroll/drag/release/move/char/key)
- gui/screen/GuiScreen.java — active tab + input override signatures (per version)
- gui/screen/impl/MainGuiScreen.java — tab bar, theme palette, input (per version)
- util/factory/DrawableElementFactory.java — createSlider/createToggle/createCheckbox/createDropdown/createSearch/createColorPicker(cb)
- di/module/impl/ServicesModule.java — registered ThemeService
- gui/drawable/element/impl/impl/singleton/{Checkouts,ReportsParser,Spy,Watermark}DrawableElement.java — use ThemePalette instead of raw guiConfig colors

## Key risks to verify
1. Version-specific APIs: 1.21.11 uses net.minecraft.client.gui.Click / CharInput / KeyInput for
   input overrides; 1.20.1 uses double/int signatures and renderBackground(DrawContext) + mouseScrolled(3 args).
2. Scissor coordinates computed via matrix transform (Matrix3x2fStack m00/m10/m20 vs Matrix4f m00()/m10()/m20()).
3. Dropdown/checkbox/search rotation via MatrixStack.multiply(RotationAxis.POSITIVE_Z...) on 1.20.1;
   Matrix3x2fStack.rotateAbout on 1.21.11.
4. Tab bar hit-testing before forwarding to active tab.
5. Both branches compile: 1.21.11 `gradlew compileJava` BUILD SUCCESSFUL; 1.20.1 (worktree) BUILD SUCCESSFUL.

## Verification commands
- 1.21.11: gradlew.bat compileJava
- 1.20.1 (worktree at C:\Users\MeYuugao\AppData\Local\Temp\opencode\hm-1.20.1): gradlew.bat compileJava

## Reviewer fixes applied (round 1)
1. GuiScreen.keyPressed / charTyped now fall back to super.keyPressed / super.charTyped when the active
   tab does not consume the event (ESC closes screen again; unhandled chars are not swallowed).
   Tab.onKeyPress / onCharTyped are now boolean; SearchDrawableElement returns true only when focused.
2. MainGuiScreen tab-bar hit-test now bounds-checks localX/localY to the panel and guards width > 0.
3. Removed unused ConfigManagerService field/param and unused Tab import from MainGuiScreen (both versions).

