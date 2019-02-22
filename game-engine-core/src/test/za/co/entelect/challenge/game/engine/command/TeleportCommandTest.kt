package za.co.entelect.challenge.game.engine.command


import za.co.entelect.challenge.game.contracts.exceptions.InvalidCommandException
import za.co.entelect.challenge.game.engine.entities.WormsMap
import za.co.entelect.challenge.game.engine.map.CellType
import za.co.entelect.challenge.game.engine.map.MapCell
import za.co.entelect.challenge.game.engine.map.Point
import za.co.entelect.challenge.game.engine.player.CommandoWorm
import za.co.entelect.challenge.game.engine.player.WormsPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TeleportCommandTest {

    @Test(expected = InvalidCommandException::class)
    fun test_apply_invalidType() {
        val testCommand = TeleportCommand(1, 1)
        val worm = CommandoWorm(10, Point(0, 0))
        val player = WormsPlayer(listOf(worm))

        val testMap = WormsMap(listOf(player), 2, 2, buildMapCells(4, CellType.DIRT))

        assertFalse(testCommand.isValid(testMap, worm))
        testCommand.execute(testMap, worm)
    }

    @Test
    fun test_apply_valid() {
        val startingPosition = Point(0, 0)
        val targetPosition = Point(1, 1)

        val testCommand = TeleportCommand(targetPosition)
        val worm = CommandoWorm(10, startingPosition)
        val player = WormsPlayer(listOf(worm))
        val testMap = WormsMap(listOf(player), 2, 2, buildMapCells(4, CellType.AIR))

        assertTrue(testCommand.isValid(testMap, worm))
        testCommand.execute(testMap, worm)

        assertEquals(testCommand.target, worm.position)
        assertEquals(testMap[testCommand.target].occupier, worm)
        assertEquals(0, worm.roundMoved)
        assertEquals(startingPosition, worm.previousPosition)
    }

    @Test(expected = InvalidCommandException::class)
    fun test_apply_nonEmpty() {
        val testCommand = TeleportCommand(1, 1)
        val worm = CommandoWorm(10, Point(0, 0))
        val player = WormsPlayer(listOf(worm))
        val testMap = WormsMap(listOf(player), 2, 2, buildMapCells(4, CellType.AIR))

        testMap[1, 1].occupier = CommandoWorm(10, Point(0, 0))

        assertFalse(testCommand.isValid(testMap, worm))
        testCommand.execute(testMap, worm)
    }

    /**
     * When two worms move to the same cell in the same round
     */
    @Test
    fun test_apply_pushback() {
        val testCommand = TeleportCommand(Point(1, 1))
        val wormA = CommandoWorm(10, Point(0, 0))
        val wormB = CommandoWorm(10, Point(2, 1))
        val player = WormsPlayer(listOf(wormA))
        val testMap = WormsMap(listOf(player), 2, 2, buildMapCells(4, CellType.AIR))

        assertTrue(testCommand.isValid(testMap, wormA), "Command A Valid")
        testCommand.execute(testMap, wormA)
        assertTrue(testCommand.isValid(testMap, wormB),"Command B Valid")
        testCommand.execute(testMap, wormB)

        assertFalse(testMap[1, 1].isOccupied(), "Target not occupied")
        assertTrue(testMap[0, 0].isOccupied())
        assertTrue(testMap[2, 1].isOccupied())
    }

    @Test
    fun test_apply_tooFar() {
        val worm = CommandoWorm(10, Point(2, 2))
        val player = WormsPlayer(listOf(worm))
        val testMap = WormsMap(listOf(player), 3, 3, buildMapCells(25, CellType.AIR))

        for (i in 0..4) {
            assertFalse(TeleportCommand(0, i).isValid(testMap, worm), "(0, $i) out of range")
            assertFalse(TeleportCommand(4, i).isValid(testMap, worm), "(4, $i) out of range")
            assertFalse(TeleportCommand(i, 0).isValid(testMap, worm), "($i, 0) out of range")
            assertFalse(TeleportCommand(i, 4).isValid(testMap, worm), "($i, 4) out of range")
        }

        for (x in 1..3) {
            for (y in 1..3) {
                assertTrue(TeleportCommand(x, y).isValid(testMap, worm), "($x, $y) in range")
            }
        }
    }

    private fun buildMapCells(count: Int, cellType: CellType) = (0..count).map { MapCell(cellType) }.toMutableList()
}
