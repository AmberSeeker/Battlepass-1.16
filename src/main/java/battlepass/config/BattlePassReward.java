package battlepass.config;

import java.util.List;

public class BattlePassReward {
  String name;
  
  String itemType;
  
  int requiredLvl;
  
  int position;
  
  int money;
  
  List<String> commands;
  
  List<String> lore;
  
  public BattlePassReward(String name, String itemType, int requiredLvl, int position, int money, List<String> commands, List<String> lore) {
    this.name = name;
    this.itemType = itemType;
    this.requiredLvl = requiredLvl;
    this.position = position;
    this.commands = commands;
    this.lore = lore;
    this.money = money;
  }
  
  public int getRequiredLvl() {
    return this.requiredLvl;
  }
  
  public void setRequiredLvl(int requiredLvl) {
    this.requiredLvl = requiredLvl;
  }
  
  public List<String> getCommands() {
    return this.commands;
  }
  
  public void setCommands(List<String> commands) {
    this.commands = commands;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getItemType() {
    return this.itemType;
  }
  
  public void setItemType(String itemType) {
    this.itemType = itemType;
  }
  
  public int getPosition() {
    return this.position;
  }
  
  public void setPosition(int position) {
    this.position = position;
  }
  
  public List<String> getLore() {
    return this.lore;
  }
  
  public void setLore(List<String> lore) {
    this.lore = lore;
  }
  
  public int getMoney() {
    return this.money;
  }
}
