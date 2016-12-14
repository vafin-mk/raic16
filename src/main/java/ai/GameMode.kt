package ai

import extensions.toPoint
import pathfinding.Lane
import pathfinding.PFType
import pathfinding.Point
import pathfinding.PotentialField
import wrapper.GameUnit

enum class GameMode {
  SINGLE_NO_SKILL {
    override fun getLane(id: Int) : Lane {
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

  },

  SINGLE_SKILL {
    override fun getLane(id: Int) : Lane {
      return Lane.MID
    }

    override fun getSkillBuild(id: Int): SkillBuild {
      return SkillBuild.FROSTDMG
    }

  },

  TEAM {
    override fun getLane(id: Int) : Lane {
      when(id) {
        1, 6 -> return Lane.TOP
        5, 10 -> return Lane.BOTTOM
        else -> return Lane.MID
      }
    }

    override fun getSkillBuild(id: Int): SkillBuild {
      when(id) {
        1, 10 -> return SkillBuild.DMGHAST
        2, 9 -> return SkillBuild.FROSTDMG
        3, 8 -> return SkillBuild.SHIELDFIRE
        4, 7 -> return SkillBuild.FROSTDMG
        5, 6 -> return SkillBuild.FIREHASTE
        else -> return SkillBuild.FROSTDMG
      }
    }

  };

  abstract fun getLane(id: Int) : Lane
  abstract fun getSkillBuild(id: Int) : SkillBuild

}