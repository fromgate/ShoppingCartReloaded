package me.limito.bukkit.shopcart

import org.bukkit.plugin.java.JavaPlugin
import java.sql.DriverManager
import java.io.File
import org.bukkit.configuration.file.YamlConfiguration
import java.util.logging.Level
import org.bukkit.command.{Command, CommandSender, CommandExecutor}
import scala.Predef.augmentString

class ShoppingCartReloaded extends JavaPlugin {
  val requestManager: RequestManager = new RequestManager(this)
  val lang = new Lang
  var dao: CartItemInfoDao = _

  override def onEnable() {
    loadMessages()
    loadItemNames()
    initDatabase()

    getServer.getPluginCommand("cart").setExecutor(this)
  }

  override def onDisable() {

  }

  def loadMessages() {
    try {
      val fileName = "messages.yml"

      saveResource(fileName, false)
      val file = new File(getDataFolder, fileName)
      val config = YamlConfiguration.loadConfiguration(file)
      lang.read(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading messages", e)
    }
  }

  def loadItemNames() {
    try {
      val fileName = "items.yml"

      saveResource(fileName, false)
      val file = new File(getDataFolder, fileName)
      val config = YamlConfiguration.loadConfiguration(file)
      lang.readItems(config)
    } catch {
      case e: Exception => getLogger.log(Level.SEVERE, s"Error loading item names", e)
    }
  }

  def initDatabase() {
    val dbConfig = loadDatabaseConfig()

    val dataSource = new JdbcDataSource(dbConfig.url, dbConfig.username,  dbConfig.password, 4)
    dao = new CartItemInfoDao(dataSource, dbConfig)
  }

  def loadDatabaseConfig(): DatabaseConfig = {
    saveDefaultConfig()
    val section = getConfig.getConfigurationSection("db")

    val url = section.getString("url")
    val username = section.getString("username")
    val password = section.getString("password")
    val table = section.getString("table")

    val columnSection = section.getConfigurationSection("column")
    val cId = columnSection.getString("id")
    val cPlayer = columnSection.getString("player")
    val cType = columnSection.getString("type")
    val cItem = columnSection.getString("item")
    val cAmount = columnSection.getString("amount")
    val cExtra = columnSection.getString("extra")

    new DatabaseConfig(url, username, password, table, cId, cType, cItem, cPlayer, cAmount, cExtra)
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean = {
    args match {
      case Array("get", itemId, itemAmount) => {
        val req = new RequestItemGive(requestManager, sender, itemId.toInt, itemAmount.toInt)
        requestManager.handleRequest(req)
        true
      }
      case Array("get", itemId) => {
        val req = new RequestItemGive(requestManager, sender, itemId.toInt, Int.MaxValue)
        requestManager.handleRequest(req)
        true
      }
      case Array("all") => {
        val req = new RequestGiveAll(requestManager, sender)
        requestManager.handleRequest(req)
        true
      }
      case Array() => {
        val req = new RequestItemsList(requestManager, sender)
        requestManager.handleRequest(req)
        true
      }
      case _ => false
    }
  }
}