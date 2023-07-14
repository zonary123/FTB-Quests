package dev.ftb.mods.ftbquests.gui.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftblibrary.util.client.PositionedIngredient;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author LatvianModder
 */
public class RewardButton extends Button {
	public final QuestScreen questScreen;
	public final Reward reward;

	public RewardButton(Panel panel, Reward r) {
		super(panel, r.getTitle(), r.getIcon());
		questScreen = (QuestScreen) panel.getGui();
		reward = r;
		setSize(18, 18);
	}

	@Override
	public Component getTitle() {
		if (reward.isTeamReward()) {
			return super.getTitle().copy().withStyle(ChatFormatting.BLUE);
		}

		return super.getTitle();
	}

	@Override
	public void addMouseOverText(TooltipList list) {
		questScreen.addInfoTooltip(list, reward);

		if (reward.addTitleInMouseOverText()) {
			list.add(getTitle());
		}

		if (reward.isTeamReward() || questScreen.file.self.isRewardBlocked(reward)) {
			getIngredientUnderMouse().ifPresent(ingredient -> {
				if (ingredient.tooltip() && ingredient.ingredient() instanceof ItemStack stack && !stack.isEmpty()) {
					List<Component> list1 = new ArrayList<>();
					GuiHelper.addStackTooltip(stack, list1);
					list1.forEach(list::add);
				}
			});

			list.blankLine();
			reward.addMouseOverText(list);
			if (reward.isTeamReward()) {
				list.add(Component.translatable("ftbquests.reward.team_reward").withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE));
			} else if (questScreen.file.self.isRewardBlocked(reward)) {
				list.add(Component.translatable("ftbquests.reward.this_blocked", questScreen.file.self).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
			}
		} else {
			reward.addMouseOverText(list);

			if (!list.shouldRender()) {
				list.zOffset = 580;
			}
		}
	}

	@Override
	public boolean mousePressed(MouseButton button) {
		if (isMouseOver()) {
			if (button.isRight() || getWidgetType() != WidgetType.DISABLED) {
				onClicked(button);
			}

			return true;
		}

		return false;
	}

	@Override
	public WidgetType getWidgetType() {
		if (!ClientQuestFile.exists() || !ClientQuestFile.INSTANCE.self.isCompleted(reward.quest)) {
			return WidgetType.DISABLED;
		}

		return super.getWidgetType();
	}

	@Override
	public void onClicked(MouseButton button) {
		if (button.isLeft()) {
			if (ClientQuestFile.exists()) {
				reward.onButtonClicked(this, ClientQuestFile.INSTANCE.self.getClaimType(Minecraft.getInstance().player.getUUID(), reward).canClaim());
			}
		} else if (button.isRight() && ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit()) {
			playClickSound();
			ContextMenuBuilder.create(reward, questScreen).openContextMenu(getGui());
		}
	}

	@Override
	public Optional<PositionedIngredient> getIngredientUnderMouse() {
		return PositionedIngredient.of(reward.getIngredient(this), this);
	}

	@Override
	public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		if (isMouseOver()) {
			super.drawBackground(graphics, theme, x, y, w, h);
		}
	}

	@Override
	public void draw(GuiGraphics graphics, Theme theme, int x, int y, int w, int h) {
		int bs = h >= 32 ? 32 : 16;
		GuiHelper.setupDrawing();
		drawBackground(graphics, theme, x, y, w, h);
		drawIcon(graphics, theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);

		if (questScreen.file.self == null) {
			return;
		} else if (questScreen.getContextMenu().isEmpty()) {
			//return;
		}

		PoseStack poseStack = graphics.pose();

		poseStack.pushPose();
		poseStack.translate(0, 0, 200);
		RenderSystem.enableBlend();
		boolean completed = false;

		if (questScreen.file.self.getClaimType(Minecraft.getInstance().player.getUUID(), reward).isClaimed()) {
			ThemeProperties.CHECK_ICON.get().draw(graphics, x + w - 9, y + 1, 8, 8);
			completed = true;
		} else if (questScreen.file.self.isCompleted(reward.quest)) {
			ThemeProperties.ALERT_ICON.get().draw(graphics, x + w - 9, y + 1, 8, 8);
		}

		poseStack.popPose();

		if (!completed) {
			String s = reward.getButtonText();

			if (!s.isEmpty()) {
				poseStack.pushPose();
				poseStack.translate(x + 19 - theme.getStringWidth(s) / 2D, y + 15, 200);
				poseStack.scale(0.5F, 0.5F, 1F);
				theme.drawString(graphics, s, 0, 0, Color4I.WHITE, Theme.SHADOW);
				poseStack.popPose();
			}
		}
	}
}
