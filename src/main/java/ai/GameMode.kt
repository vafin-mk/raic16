package ai

import extensions.toPoint
import pathfinding.Lane
import pathfinding.PFType
import pathfinding.Point
import pathfinding.PotentialField
import wrapper.GameUnit
import java.util.*

enum class GameMode {
  SINGLE_NO_SKILL {
    override fun getLane(id: Int, random: Random) : Lane {
      when(id) {
        1, 2, 6, 7 -> return Lane.TOP
        4, 5, 9, 10 -> return Lane.BOTTOM
        3, 8 -> return Lane.MID
        else -> return Lane.MID
      }
    }

    override fun getSkillBuild(id: Int): SkillBuild {
      return SkillBuild.FROSTDMG
    }

    override fun getCoefficent(type: CoefType): Double {
      return when(type) {
        CoefType.RETREAT_HP_PERCENT_THRESHOLD -> 45.0
        CoefType.ENEMY_WIZARD_HUNT_HP_PERCENT_THRESHOLD -> 15.0
        CoefType.ENEMY_BUILDING_HUNT_HP_PERCENT_THRESHOLD -> 5.0
        else -> TODO()
      }
    }
  },

  SINGLE_SKILL {
    override fun getLane(id: Int, random: Random) : Lane {
      return Lane.MID
    }

    override fun getSkillBuild(id: Int): SkillBuild {
      return SkillBuild.FROSTDMG
    }

    override fun getCoefficent(type: CoefType): Double {
      return when(type) {
        CoefType.RETREAT_HP_PERCENT_THRESHOLD -> 20.0
        CoefType.ENEMY_WIZARD_HUNT_HP_PERCENT_THRESHOLD -> 30.0
        CoefType.ENEMY_BUILDING_HUNT_HP_PERCENT_THRESHOLD -> 10.0
        else -> TODO()
      }
    }
  },

  TEAM {
    override fun getLane(id: Int, random: Random) : Lane {
      when(id) {
        1, 6 -> return Lane.TOP
        5, 10 -> return Lane.BOTTOM
        else -> return Lane.MID.randomize(random, 25.0)
      }
    }

    override fun getSkillBuild(id: Int): SkillBuild {
      when(id) {
        1, 6 -> return SkillBuild.DMGFROST
        2, 7 -> return SkillBuild.FROSTDMG
        3, 8 -> return SkillBuild.FIRESHIELD
        4, 9 -> return SkillBuild.FROSTHASTE
        5, 10 -> return SkillBuild.FROSTDMG
        else -> return SkillBuild.FROSTDMG
      }
    }

    override fun getCoefficent(type: CoefType): Double {
      return when(type) {
        CoefType.RETREAT_HP_PERCENT_THRESHOLD -> 25.0
        CoefType.ENEMY_WIZARD_HUNT_HP_PERCENT_THRESHOLD -> 35.0
        CoefType.ENEMY_BUILDING_HUNT_HP_PERCENT_THRESHOLD -> 20.0
        else -> TODO()
      }
    }
  };

  abstract fun getLane(id: Int, random: Random) : Lane
  abstract fun getSkillBuild(id: Int) : SkillBuild
  abstract fun getCoefficent(type: CoefType) : Double

}