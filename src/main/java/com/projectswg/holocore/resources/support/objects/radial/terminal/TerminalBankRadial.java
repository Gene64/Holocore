package com.projectswg.holocore.resources.support.objects.radial.terminal;

import com.projectswg.common.data.radial.RadialItem;
import com.projectswg.common.data.radial.RadialOption;
import com.projectswg.common.data.sui.SuiEvent;
import com.projectswg.common.network.packets.swg.zone.ClientOpenContainerMessage;
import com.projectswg.holocore.intents.support.global.chat.SystemMessageIntent;
import com.projectswg.holocore.resources.support.global.player.Player;
import com.projectswg.holocore.resources.support.global.zone.sui.SuiButtons;
import com.projectswg.holocore.resources.support.global.zone.sui.SuiWindow;
import com.projectswg.holocore.resources.support.objects.radial.RadialHandlerInterface;
import com.projectswg.holocore.resources.support.objects.swg.SWGObject;
import com.projectswg.holocore.resources.support.objects.swg.creature.CreatureObject;
import com.projectswg.holocore.resources.support.objects.swg.player.PlayerObject;
import com.projectswg.holocore.utilities.IntentFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TerminalBankRadial implements RadialHandlerInterface {
	
	public TerminalBankRadial() {
		
	}
	
	@Override
	public void getOptions(Collection<RadialOption> options, Player player, SWGObject target) {
		CreatureObject creature = player.getCreatureObject();
		PlayerObject playerObject = player.getPlayerObject();
		
		{
			List<RadialOption> useOptions = new ArrayList<>();
			
			// Bank Transfer/Safety Deposit
			useOptions.add(RadialOption.create(RadialItem.SERVER_MENU1, "@sui:bank_credits"));
			useOptions.add(RadialOption.create(RadialItem.SERVER_MENU2, "@sui:bank_items"));
			// Withdraw/Deposit
			if (creature.getBankBalance() > 0)
				useOptions.add(RadialOption.create(RadialItem.SERVER_MENU4, "@sui:bank_withdrawall"));
			if (creature.getCashBalance() > 0)
				useOptions.add(RadialOption.create(RadialItem.SERVER_MENU3, "@sui:bank_depositall"));
			
			options.add(RadialOption.createSilent(RadialItem.ITEM_USE, useOptions));
		}
		
		if (isInGalacticReserveCity(creature)) {
			List<RadialOption> reserveOptions = new ArrayList<>();
			if (creature.getBankBalance() >= 1E9 || creature.getCashBalance() >= 1E9)
				reserveOptions.add(RadialOption.create(RadialItem.SERVER_MENU49, "@sui:bank_galactic_reserve_deposit"));
			if (playerObject != null && playerObject.getGalacticReserveDeposit() > 0)
				reserveOptions.add(RadialOption.create(RadialItem.SERVER_MENU48, "@sui:bank_galactic_reserve_withdraw"));
			
			options.add(RadialOption.createSilent(RadialItem.SERVER_MENU50, "@sui:bank_galactic_reserve", reserveOptions));
		}
	}
	
	@Override
	public void handleSelection(Player player, SWGObject target, RadialItem selection) {
		CreatureObject creature = player.getCreatureObject();
		switch (selection) {
			case ITEM_USE:
			case SERVER_MENU1:
				handleTransfer(player, creature);
				break;
			case SERVER_MENU2:
				handleOpenBank(player, creature);
				break;
			case SERVER_MENU3:
				handleBankDeposit(player, creature);
				break;
			case SERVER_MENU4:
				handleBankWithdraw(player, creature);
				break;
			case SERVER_MENU49:
				handleGalacticReserveDeposit(player, creature);
				break;
			case SERVER_MENU48:
				handleGalacticReserveWithdraw(player, creature);
				break;
			default:
				break;
		}
	}
	
	private static void handleTransfer(Player player, CreatureObject creature) {
		SuiWindow window = new SuiWindow("Script.transfer", SuiButtons.OK_CANCEL, "@base_player:bank_title", "@base_player:bank_prompt");
		window.setPropertyText("transaction.lblFrom", "Cash");
		window.setPropertyText("transaction.lblTo", "Bank");
		window.setPropertyText("transaction.lblStartingFrom", Long.toString(creature.getCashBalance()));
		window.setPropertyText("transaction.lblStartingTo", Long.toString(creature.getBankBalance()));
		window.setPropertyText("transaction.txtInputFrom", Long.toString(creature.getCashBalance()));
		window.setPropertyText("transaction.txtInputTo", Long.toString(creature.getBankBalance()));
		window.setProperty("transaction", "ConversionRatioFrom", "1");
		window.setProperty("transaction", "ConversionRatioTo", "1");
		window.addReturnableProperty("transaction.txtInputFrom", "Text");
		window.addReturnableProperty("transaction.txtInputTo", "Text");
		window.addCallback(SuiEvent.OK_PRESSED, "handleBankTransfer", (event, parameters) -> handleBankTransfer(player, player.getCreatureObject(), parameters));
		window.display(player);
	}
	
	private static void handleOpenBank(Player player, CreatureObject creature) {
		player.sendPacket(new ClientOpenContainerMessage(creature.getSlottedObject("bank").getObjectId(), ""));
	}
	
	private static void handleBankDeposit(Player player, CreatureObject creature) {
		long amount = creature.getCashBalance();
		creature.setBankBalance(amount + creature.getBankBalance());
		creature.setCashBalance(0L);
		if (amount > 0)
			IntentFactory.sendSystemMessage(player, "@base_player:prose_deposit_success", "DI", (int) amount);
		else
			SystemMessageIntent.broadcastPersonal(player, "@error_message:bank_deposit");
	}
	
	private static void handleBankWithdraw(Player player, CreatureObject creature) {
		long amount = creature.getBankBalance();
		creature.setCashBalance(creature.getCashBalance() + amount);
		creature.setBankBalance(0L);
		if (amount > 0)
			IntentFactory.sendSystemMessage(player, "@base_player:prose_withdraw_success", "DI", (int) amount);
		else
			SystemMessageIntent.broadcastPersonal(player, "@error_message:bank_withdraw");
	}
	
	private static boolean isInGalacticReserveCity(CreatureObject creature) {
		return creature.getCurrentCity().equals("@corellia_region_names:coronet")
				|| creature.getCurrentCity().equals("@naboo_region_names:theed")
				|| creature.getCurrentCity().equals("@tatooine_region_names:mos_eisley");
	}
	
	private static void handleGalacticReserveDeposit(Player player, CreatureObject creature) {
		if (!creature.canPerformGalacticReserveTransaction()) {
			SystemMessageIntent.broadcastPersonal(player, "You have to wait to perform another Galactic Reserve transaction");
			return;
		}
		PlayerObject playerObject = player.getPlayerObject();
		if (playerObject == null) {
			SystemMessageIntent.broadcastPersonal(player, "Internal server error with loading your player");
			return;
		}
		long updatedBank = creature.getBankBalance() - 1_000_000_000L;
		byte reserveBalance = playerObject.getGalacticReserveDeposit();
		if (reserveBalance >= 3 || updatedBank < 0) {
			SystemMessageIntent.broadcastPersonal(player, "@error_message:bank_deposit");
			return;
		}
		creature.setBankBalance(updatedBank);
		playerObject.setGalacticReserveDeposit((byte) (reserveBalance + 1));
		creature.updateLastGalacticReserveTime();
	}
	
	private static void handleGalacticReserveWithdraw(Player player, CreatureObject creature) {
		if (!creature.canPerformGalacticReserveTransaction()) {
			SystemMessageIntent.broadcastPersonal(player, "You have to wait to perform another Galactic Reserve transaction");
			return;
		}
		PlayerObject playerObject = player.getPlayerObject();
		if (playerObject == null) {
			SystemMessageIntent.broadcastPersonal(player, "Internal server error with loading your player");
			return;
		}
		long updatedBank = creature.getBankBalance() + 1_000_000_000L;
		byte reserveBalance = playerObject.getGalacticReserveDeposit();
		if (reserveBalance <= 0 || updatedBank >= 2_000_000_000L) {
			SystemMessageIntent.broadcastPersonal(player, "@error_message:bank_deposit");
			return;
		}
		creature.setBankBalance(updatedBank);
		playerObject.setGalacticReserveDeposit((byte) (reserveBalance - 1));
		creature.updateLastGalacticReserveTime();
	}
	
	private static void handleBankTransfer(Player player, CreatureObject creature, Map<String, String> parameters) {
		creature.setCashBalance(Long.parseLong(parameters.get("transaction.txtInputFrom.Text")));
		creature.setBankBalance(Long.parseLong(parameters.get("transaction.txtInputTo.Text")));
		SystemMessageIntent.broadcastPersonal(player, "@base_player:bank_success");
	}
	
}
