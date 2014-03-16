package me.limito.bukkit.shopcart.items

import org.bukkit.entity.Player
import org.bukkit.inventory.{Inventory, ItemStack}
import me.limito.bukkit.shopcart.Lang
import org.bukkit.enchantments.Enchantment

class CartItemItem(info: CartItemInfo, val itemId: Int, val itemMeta: Short, val amount: Int, val enchantments: Array[LeveledEnchantment], val nbtData: String) extends CartItem(info) {
  def giveToPlayer(player: Player):Int = giveToPlayer(player, amount)

  def giveToPlayer(player: Player, amount: Int):Int = {
    val stack = new ItemStack(itemId, 1, itemMeta)
    if (enchantments != null)
      enchantments foreach(e => stack.addUnsafeEnchantment(Enchantment.getById(e.id), e.level))

    give(player.getInventory, stack, amount)
  }

  def getLocalizedName(lang: Lang): String = {
    val name = lang.getItemName(itemId, itemMeta)
    if (enchantments != null)
      name + " " + lang.formatEnchantments(enchantments)
    else
      name
  }

  def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get", getLocalizedName(lang), amount)

  private def give(inv: Inventory, baseStack: ItemStack, amount: Int): Int = {
    require(amount >= 0)
    if (amount == 0)
      return 0

    val maxStackSize = Math.max(baseStack.getMaxStackSize, 1)
    val amountToGive = Math.min(maxStackSize, amount)
    val stack = baseStack.clone()
    stack.setAmount(amountToGive)
    val notGiven = inv.addItem(stack)

    if (notGiven.isEmpty)
      amountToGive + give(inv, baseStack, amount - amountToGive)
    else
      amountToGive - notGiven.values().iterator().next().getAmount
  }
}