package extensions

import model.ActionType
import model.SkillType
import model.Wizard
import pathfinding.Point

fun Wizard.canFight() : Boolean {
  return remainingActionCooldownTicks <= 0
}

//todo manacost
fun Wizard.canUse(action : ActionType) : Boolean {
  if (!canFight()) {
    return false
  }
  when(action) {
    ActionType.MAGIC_MISSILE -> getRemainingCooldownTicksByAction()[action.ordinal] > 0
    ActionType.STAFF -> getRemainingCooldownTicksByAction()[action.ordinal] > 0
    ActionType.FIREBALL -> getRemainingCooldownTicksByAction()[action.ordinal] > 0 && skillLearned(SkillType.FIREBALL)
    ActionType.FROST_BOLT -> getRemainingCooldownTicksByAction()[action.ordinal] > 0 && skillLearned(SkillType.FROST_BOLT)
    ActionType.HASTE -> getRemainingCooldownTicksByAction()[action.ordinal] > 0 && skillLearned(SkillType.HASTE)
    ActionType.SHIELD -> getRemainingCooldownTicksByAction()[action.ordinal] > 0 && skillLearned(SkillType.SHIELD)
    ActionType.NONE -> return true
  }
  return false
}

fun Wizard.skillLearned(skill: SkillType) : Boolean {
  return getSkills().contains(skill)
}

fun model.Unit.toPoint() : Point{
  return Point(x, y)
}