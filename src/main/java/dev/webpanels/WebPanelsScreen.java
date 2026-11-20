package dev.webpanels;

import com.cinemamod.mcef.MCEFBrowser;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class WebPanelsScreen extends Screen {
    private MCEFBrowser browser;
    private TextFieldWidget address;
    private ButtonWidget go;
    private int browserX, browserY, browserW, browserH;

    public WebPanelsScreen() { super(Text.literal("WebPanels")); }

    @Override
    protected void init() {
        int margin = 18;
        int barY = 34;
        browserX = margin;
        browserY = 66;
        browserW = this.width - margin * 2;
        browserH = this.height - browserY - margin;

        address = new TextFieldWidget(textRenderer, margin, barY, this.width - 118, 20, Text.literal("Address"));
        address.setMaxLength(2048);
        address.setText("https://www.youtube.com/");
        addDrawableChild(address);
        go = ButtonWidget.builder(Text.literal("Go"), b -> navigate()).dimensions(this.width - 92, barY, 74, 20).build();
        addDrawableChild(go);

        browser = WebPanelsClient.createBrowser(address.getText(), browserW, browserH);
        if (browser != null) browser.resize(browserW, browserH);
    }

    private void navigate() {
        if (browser == null) return;
        String value = address.getText().trim();
        if (value.isEmpty()) return;
        if (!value.matches("(?i)^https?://.*")) value = "https://" + value;
        address.setText(value);
        browser.loadURL(value);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE810121A);
        context.fill(12, 12, width - 12, height - 12, 0xFF202633);
        context.drawTextWithShadow(textRenderer, "WEBPANELS", 18, 16, 0xFF79CFFF);
        if (browser != null && browser.isTextureReady()) {
            context.drawTexture(net.minecraft.client.render.RenderLayer::getGuiTextured,
                    browser.getTextureIdentifier(), browserX, browserY,
                    0.0f, 0.0f, browserW, browserH, browserW, browserH);
        } else {
            context.fill(browserX, browserY, browserX + browserW, browserY + browserH, 0xFF11151D);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(browser == null ? "MCEF browser is not ready" : "Loading browser…"),
                    width / 2, browserY + browserH / 2, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (browser != null && mouseX >= browserX && mouseX < browserX + browserW
                && mouseY >= browserY && mouseY < browserY + browserH) {
            browser.sendMouseMove((int) mouseX - browserX, (int) mouseY - browserY);
            browser.sendMousePress((int) mouseX - browserX, (int) mouseY - browserY, button);
            browser.sendMouseRelease((int) mouseX - browserX, (int) mouseY - browserY, button);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseMoved(double mouseX, double mouseY) {
        if (browser != null && mouseX >= browserX && mouseX < browserX + browserW
                && mouseY >= browserY && mouseY < browserY + browserH) {
            browser.sendMouseMove((int) mouseX - browserX, (int) mouseY - browserY);
        }
        return super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (browser != null && mouseX >= browserX && mouseX < browserX + browserW
                && mouseY >= browserY && mouseY < browserY + browserH) {
            browser.sendMouseWheel((int) mouseX - browserX, (int) mouseY - browserY, verticalAmount * 120.0, 0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && address.isFocused()) { navigate(); return true; }
        if (browser != null && !address.isFocused()) {
            browser.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (browser != null && !address.isFocused()) browser.sendKeyRelease(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (browser != null && !address.isFocused()) { browser.sendKeyTyped(chr, modifiers); return true; }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        if (browser != null) { browser.close(); browser = null; }
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
