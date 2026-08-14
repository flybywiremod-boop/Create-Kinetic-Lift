package net.flybywire.createkineticlift.foundation.gui;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;

import net.flybywire.createkineticlift.CreateKineticLift;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum CKLGuiTextures implements ScreenElement, TextureSheetSegment {

	//Chair
	SEAT_LINK("ChairLink", 0, 0, 131, 109),
	SEAT_BIN_PRESSED("ChairLink",0,109,18,18),
	SEAT_BIN("ChairLink",18,109,18,18),



	// CHAIR_ENG("chair_information",  ???, ???,),
	// CHAIR_ENG_REV("chair_information",  ???, ???,),
	// CHAIR_ENG_POINTER("chair_information",  ???, ???,),
	// CHAIR_ENG_NOTCH("chair_information",  ???, ???,),

	// CHAIR_ALTITUDE_LINE("chair_information",  ???, ???,),
	// CHAIR_ALTITUDE_INDICATOR("chair_information",  ???, ???,),

	// CHAIR_AIRSPEED_INDICATOR("chair_information",  ???, ???,),
	// CHAIR_AIRSPEED_LINE("chair_information",  ???, ???,),
	// CHAIR_AIRSPEED_WHITE("chair_information",  ???, ???,),
	// CHAIR_AIRSPEED_GREEN("chair_information",  ???, ???,),
	// CHAIR_AIRSPEED_YELLOW("chair_information",  ???, ???,),
	// CHAIR_AIRSPEED_RED("chair_information",  ???, ???,),
	// CHAIR_AIRSPEED("chair_information",  ???, ???,),

	//CHAIR_FLAPS("chair_information",  ???, ???,),

	//CHAIR_FLAPS_FUEL_POINTER("chair_information",  ???, ???,),

	//CHAIR_FUEL("chair_information",  ???, ???,),

	// Chairs AOA
	//CHAIR_ANGLE_OF_ATTACK1("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK2("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK3("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK4("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK5("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK6("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK7("chair_information",  ???, ???,),
	//CHAIR_ANGLE_OF_ATTACK8("chair_information",  ???, ???,)

	;

	public static final int FONT_COLOR = 0x575F7A;

	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;

	CKLGuiTextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}

	CKLGuiTextures(String location, int startX, int startY, int width, int height) {
		this(CreateKineticLift.MOD_ID, location, startX, startY, width, height);
	}

	CKLGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
		this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	@Override
	public ResourceLocation getLocation() {
		return location;
	}

	@OnlyIn(Dist.CLIENT)
	public void render(GuiGraphics graphics, int x, int y) {
		graphics.blit(location, x, y, startX, startY, width, height);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(GuiGraphics graphics, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
	}

	@Override
	public int getStartX() {
		return startX;
	}

	@Override
	public int getStartY() {
		return startY;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
