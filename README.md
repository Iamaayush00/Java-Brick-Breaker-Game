# Java Brick Breaker Game

An enhanced, modern implementation of the classic Brick Breaker arcade game built with Java Swing. Features smooth animations, particle effects, progressive difficulty, and power-ups.

## Features

- **Unlimited Progressive Levels** - Each level adds more bricks and increases difficulty
- **Particle Effects** - Visual feedback when bricks are destroyed
- **Power-Ups System** - Collect random power-ups for advantages:
  - Extra Life
  - Score Boost
  - Slow Ball
- **Modern Graphics** - Smooth gradients, glowing effects, and polished UI
- **Dynamic Physics** - Ball angle changes based on where it hits the paddle
- **Lives & Scoring System** - Start with 3 lives, score multiplies with level
- **Pause Functionality** - Pause and resume gameplay anytime
- **Multiple Game States** - Menu, playing, paused, game over, and level complete screens

## How to Play

### Controls
- **Arrow Keys (Left/Right)** - Move the paddle
- **SPACE** - Launch the ball / Pause game
- **ENTER** - Start game / Continue to next level / Restart after game over
- **ESC** - Return to main menu

### Objective
- Break all the bricks to complete each level
- Don't let the ball fall off the bottom of the screen
- Collect power-ups for bonuses
- Survive as many levels as you can!

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- A terminal/command prompt

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone https://github.com/Iamaayush00/Java-Brick-Breaker-Game.git
   cd Java-Brick-Breaker-Game
   ```

2. **Compile the game**
   ```bash
   javac BrickBreaker.java
   ```

3. **Run the game**
   ```bash
   java BrickBreaker
   ```

### Alternative: Run Directly
If you just want to run it quickly:
```bash
java BrickBreaker.java
```
(Java 11+ supports running single-file programs directly)

## Gameplay

### Scoring
- Each brick destroyed: **10 points × current level**
- Score Boost power-up: **+50 points × current level**

### Levels
- **Level 1**: 3 rows of bricks
- **Level 2**: 4 rows of bricks
- **Level 3**: 5 rows of bricks
- **And so on...** (caps at 8 rows)

### Power-Ups (15% drop chance)
| Icon | Name | Effect |
|------|------|--------|
| Heart | Extra Life | Gain one additional life |
| Star | Score Boost | Instant score bonus |
| Circle | Slow Ball | Reduces ball speed for easier control |

## Screenshots

_Screenshots coming soon!_

### Ideas for Enhancement
- Add sound effects and background music
- Implement high score persistence (file I/O)
- Add different brick types (multi-hit, explosive, indestructible)
- Create custom level patterns
- Add boss levels every 5 levels
- Implement multi-ball power-up
- Add mobile controls support

## Author

**Aayush Aggarwal**
- GitHub: [@Iamaayush00](https://github.com/Iamaayush00)

## Acknowledgments

- Inspired by the classic Atari Breakout arcade game
- Built as a learning project to demonstrate Java game development

---

**Star this repository if you found it helpful!**