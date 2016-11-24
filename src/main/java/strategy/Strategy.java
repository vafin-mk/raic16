package strategy;

import model.Game;
import model.Move;
import model.Wizard;
import model.World;

/**
 * Стратегия --- интерфейс, содержащий описание методов искусственного интеллекта волшебника.
 * Каждая пользовательская стратегия должна реализовывать этот интерфейс.
 * Может отсутствовать в некоторых языковых пакетах, если язык не поддерживает интерфейсы.
 */
public interface Strategy {
  /**
   * Основной метод стратегии, осуществляющий управление волшебником.
   * Вызывается каждый тик для каждого волшебника.
   *
   * @param self  Волшебник, которым данный метод будет осуществлять управление.
   * @param world Текущее состояние мира.
   * @param game  Различные игровые константы.
   * @param move  Результатом работы метода является изменение полей данного объекта.
   */
  void move(Wizard self, World world, Game game, Move move);
}