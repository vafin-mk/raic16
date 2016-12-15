package ai

import model.SkillType
import model.StatusType

class SkillBuild(vararg skills : SkillType) {

  val build = skills

  companion object {
    private val FIRE_BRANCH = arrayOf(SkillType.STAFF_DAMAGE_BONUS_PASSIVE_1,
        SkillType.STAFF_DAMAGE_BONUS_AURA_1,
        SkillType.STAFF_DAMAGE_BONUS_PASSIVE_2,
        SkillType.STAFF_DAMAGE_BONUS_AURA_2,
        SkillType.FIREBALL)

    private val FROST_BRANCH = arrayOf(SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_1,
        SkillType.MAGICAL_DAMAGE_BONUS_AURA_1,
        SkillType.MAGICAL_DAMAGE_BONUS_PASSIVE_2,
        SkillType.MAGICAL_DAMAGE_BONUS_AURA_2,
        SkillType.FROST_BOLT)

    private val MINIGUN_BRANCH = arrayOf(SkillType.RANGE_BONUS_PASSIVE_1,
        SkillType.RANGE_BONUS_AURA_1,
        SkillType.RANGE_BONUS_PASSIVE_2,
        SkillType.RANGE_BONUS_AURA_2,
        SkillType.ADVANCED_MAGIC_MISSILE)
    private val HASTE_BRANCH = arrayOf(SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_1,
        SkillType.MOVEMENT_BONUS_FACTOR_AURA_1,
        SkillType.MOVEMENT_BONUS_FACTOR_PASSIVE_2,
        SkillType.MOVEMENT_BONUS_FACTOR_AURA_2,
        SkillType.HASTE)
    private val SHIELD_BRANCH = arrayOf(SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_1,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_1,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_PASSIVE_2,
        SkillType.MAGICAL_DAMAGE_ABSORPTION_AURA_2,
        SkillType.SHIELD)

    val FROSTDMG = SkillBuild (*FROST_BRANCH, *MINIGUN_BRANCH, *HASTE_BRANCH, *FIRE_BRANCH, *SHIELD_BRANCH)
    val FIRESHIELD = SkillBuild(*FIRE_BRANCH, *SHIELD_BRANCH, *MINIGUN_BRANCH, *HASTE_BRANCH, *FROST_BRANCH)
    val DMGFROST = SkillBuild(*MINIGUN_BRANCH, *FROST_BRANCH, *SHIELD_BRANCH, *HASTE_BRANCH, *FIRE_BRANCH)
    val FROSTHASTE = SkillBuild(*FROST_BRANCH, *HASTE_BRANCH, *MINIGUN_BRANCH, *FIRE_BRANCH, *SHIELD_BRANCH)
  }
}