package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.Lang
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import net.milkbowl.vault.permission.Permission

class CartItemPermGroup(groupName: String, worldName: Option[String] = None) extends CartItem {
  override def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get-permgroup", groupName)

  override def getLocalizedName(lang: Lang): String = lang.format("cart.perm", groupName)


  override def giveToPlayer(player: Player, amount: Int): Int = {
    val rsp = Bukkit.getServer.getServicesManager.getRegistration(classOf[Permission])
    if (rsp != null) {
      val permProvider = rsp.getProvider
      val worldNameOrNull = worldName.getOrElse(null)

      if (!permProvider.playerInGroup(worldNameOrNull, player.getName, groupName)) {
        permProvider.playerAddGroup(player, groupName)
        1
      } else 0
    } else 0
  }

  override def giveToPlayer(player: Player): Int = giveToPlayer(player, 1)
}
