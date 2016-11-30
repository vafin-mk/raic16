package ai

import model.SkillType
import model.StatusType

class SkillBuild(vararg skills : SkillType) {

  val build = skills

  companion object {
    val SIMPLE = SkillBuild(
        SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_1,
        SkillType.MAGICAL_DAMAGE_BONUS_AURA_1,
        SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_2,
        SkillType.MAGICAL_DAMAGE_BONUS_AURA_2,
        SkillType.FROST_BOLT,
        SkillType.STAFF_DAMAGE_BONUS_PASSIVE_1,
        SkillType.STAFF_DAMAGE_BONUS_AURA_1,
        SkillType.STAFF_DAMAGE_BONUS_PASSIVE_2,
        SkillType.STAFF_DAMAGE_BONUS_AURA_2,
        SkillType.FIREBALL,
        SkillType.RANGE_BONUS_PASSIVE_1,
        SkillType.RANGE_BONUS_AURA_1,
        SkillType.RANGE_BONUS_PASSIVE_2,
        SkillType.RANGE_BONUS_AURA_2,
        SkillType.ADVANCED_MAGIC_MISSILE,
        SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_1,
        SkillType.MOVEMENT_BONUS_FACTOR_AURA_1,
        SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_2,
        SkillType.MOVEMENT_BONUS_FACTOR_AURA_2,
        SkillType.HASTE,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_1,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_1,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_2,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_2,
        SkillType.SHIELD
    )
  }
}