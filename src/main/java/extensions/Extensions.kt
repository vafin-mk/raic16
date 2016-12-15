package extensions

import model.*
import pathfinding.Point

fun Wizard.canFight() : Boolean {
  return remainingActionCooldownTicks == 0
}

//todo manacost
fun Wizard.canUse(action : ActionType, game: Game) : Boolean {
  if (!canFight()) {
    return false
  }
  when(action) {
    ActionType.MAGIC_MISSILE -> return getRemainingCooldownTicksByAction()[ActionType.MAGIC_MISSILE.ordinal] == 0 && mana > game.magicMissileManacost
    ActionType.STAFF -> return getRemainingCooldownTicksByAction()[ActionType.STAFF.ordinal] == 0
    ActionType.FIREBALL -> return getRemainingCooldownTicksByAction()[ActionType.FIREBALL.ordinal] == 0
        && skillLearned(SkillType.FIREBALL) && mana > game.fireballManacost
    ActionType.FROST_BOLT -> return getRemainingCooldownTicksByAction()[ActionType.FROST_BOLT.ordinal] == 0
        && skillLearned(SkillType.FROST_BOLT) && mana > game.frostBoltManacost
    ActionType.HASTE -> return getRemainingCooldownTicksByAction()[ActionType.HASTE.ordinal] == 0
        && skillLearned(SkillType.HASTE) && mana > game.hasteManacost
    ActionType.SHIELD -> return getRemainingCooldownTicksByAction()[ActionType.SHIELD.ordinal] == 0
        && skillLearned(SkillType.SHIELD) && mana > game.shieldManacost
    ActionType.NONE -> return true
  }
}

fun Wizard.skillLearned(skill: SkillType) : Boolean {
  return getSkills().contains(skill)
}

fun model.Unit.toPoint() : Point{
  return Point(x, y)
}

fun Faction.opposite() : Faction{
  when(this) {
    Faction.ACADEMY -> return Faction.RENEGADES
    Faction.RENEGADES -> return Faction.ACADEMY
    else -> return this
  }
}

fun LivingUnit.hastened() : Boolean {
  return getStatuses().find { it.type == StatusType.HASTENED && it.remainingDurationTicks > 5} != null
}

fun LivingUnit.burning() : Boolean {
  return getStatuses().find { it.type == StatusType.BURNING } != null
}

fun LivingUnit.shielded() : Boolean {
  return getStatuses().find { it.type == StatusType.SHIELDED && it.remainingDurationTicks > 5} != null
}

fun LivingUnit.empowered() : Boolean {
  return getStatuses().find { it.type == StatusType.EMPOWERED } != null
}

fun LivingUnit.frozen() : Boolean {
  return getStatuses().find { it.type == StatusType.FROZEN && it.remainingDurationTicks > 5 } != null
}

fun LivingUnit.hpPercent() : Double {
  return life.toDouble() * 100 / maxLife
}

fun Wizard.mpPercent() : Double {
  return mana.toDouble() * 100 / maxMana
}