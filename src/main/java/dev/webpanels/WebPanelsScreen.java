package dev.webpanels;

import com.cinemamod.mcef.MCEFBrowser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class WebPanelsScreen extends Screen {
    private MCEFBrowser browser;
    private TextFieldWidget address;
    private ButtonWidget go;
    private int browserX, browserY, browserW, browserH;
    private double scale = 1.0;

    public WebPanelsScreen() { super(Text.literal("WebPanels")); }

    private int pixelW() { return Math.max(1, (int) Math.round(browserW * scale)); }
    private int pixelH() { return Math.max(1, (int) Math.round(browserH * scale)); }
    private int bx(double mouseX) { return (int) Math.round((mouseX - browserX) * scale); }
    private int by(double mouseY) { return (int) Math.round((mouseY - browserY) * scale); }

    @Override
    protected void init() {
        int margin = 18;
        int barY = 34;
        browserX = margin;
        browserY = 66;
        browserW = this.width - margin * 2;
        browserH = this.height - browserY - margin;
        scale = this.client.getWindow().getScaleFactor();

        address = new TextFieldWidget(textRenderer, margin, barY, this.width - 118, 20, Text.literal("Address"));
        address.setMaxLength(2048);
        address.setText("https://www.youtube.com/");
        addDrawableChild(address);
        go = ButtonWidget.builder(Text.literal("Go"), b -> navigate()).dimensions(this.width - 92, barY, 74, 20).build();
        addDrawableChild(go);

        if (browser == null) {
            browser = WebPanelsClient.createBrowser(address.getText(), pixelW(), pixelH());
        }
        if (browser != null) browser.resize(pixelW(), pixelH());
    }

    private void navigate() {
        if (browser == null) return;
        String value = address.getText().trim();
        if (value.isEmpty()) return;
        if (!value.matches("(?i)^https?://.*")) value = "https://" + value;
        address.setText(value);
        browser.loadURL(value);
    }

    private boolean textureReady() {
        return browser != null && browser.getRenderer() != null && browser.getRenderer().getTextureID() != 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE810121A);
        context.fill(12, 12, width - 12, height - 12, 0xFF202633);
        context.drawTextWithShadow(textRenderer, "WEBPANELS", 18, 16, 0xFF79CFFF);
        if (textureReady()) {
            float x1 = browserX, y1 = browserY, x2 = browserX + browserW, y2 = browserY + browserH;
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, browser.getRenderer().getTextureID());
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(x1, y2, 0.0f).texture(0.0f, 1.0f).color(255, 255, 255, 255);
            buffer.vertex(x2, y2, 0.0f).texture(1.0f, 1.0f).color(255, 255, 255, 255);
            buffer.vertex(x2, y1, 0.0f).texture(1.0f, 0.0f).color(255, 255, 255, 255);
            buffer.vertex(x1, y1, 0.0f).texture(0.0f, 0.0f).color(255, 255, 255, 255);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.enableDepthTest();
        } else {
            context.fill(browserX, browserY, browserX + browserW, browserY + browserH, 0xFF11151D);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(browser == null ? "MCEF browser is not ready" : "Loading browser..."),
                    width / 2, browserY + browserH / 2, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private boolean inBrowser(double mouseX, double mouseY) {
        return browser != null && mouseX >= browserX && mouseX < browserX + browserW
                && mouseY >= browserY && mouseY < browserY + browserH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inBrowser(mouseX, mouseY)) {
            address.setFocused(false);
            int x = bx(mouseX), y = by(mouseY);
            browser.sendMouseMove(x, y);
            browser.sendMousePress(x, y, button);
            browser.sendMouseRelease(x, y, button);
            browser.setFocus(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (inBrowser(mouseX, mouseY)) {
            browser.sendMouseMove(bx(mouseX), by(mouseY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (inBrowser(mouseX, mouseY)) {
            browser.sendMouseWheel(bx(mouseX), by(mouseY), verticalAmount, 0);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (keyCode == GLFW.GLFW_KEY_ENTER && address.isFocused()) { navigate(); return true; }
        if (browser != null && !address.isFocused()) {
            browser.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return true;
        if (browser != null && !address.isFocused()) {
            browser.sendKeyRelease(keyCode, scanCode, modifiers);
            return true;
        }
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
