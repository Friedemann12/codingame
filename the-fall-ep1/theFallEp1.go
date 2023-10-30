package main

import (
	"bufio"
	"errors"
	"fmt"
	"os"
	"strconv"
	"strings"
)

/**
┏┳┓┓┏┳┏┓  ┏┓┓┏┳┏┳┓  ┳┏┓  ┏┓┳┳┳┓┏┓┏┓┳┓
 ┃ ┣┫┃┗┓  ┗┓┣┫┃ ┃   ┃┗┓  ┃ ┃┃┣┫┗┓┣ ┃┃
 ┻ ┛┗┻┗┛  ┗┛┛┗┻ ┻   ┻┗┛  ┗┛┗┛┛┗┗┛┗┛┻┛
**/

type tileType struct {
	paths []path
	id    int
}

type tile struct {
	tileType tileType
	x        int
	y        int
}

type path struct {
	in  DIR
	out DIR
}

type DIR int

const (
	TOP    DIR = 0
	RIGHT  DIR = 1
	BOTTOM DIR = 2
	LEFT   DIR = 3
)

func contains(s []tile, str tile) bool {
	for _, v := range s {
		if str.x == v.x && str.y == v.y {
			return true
		}
	}
	return false
}

func getNeighbours(tile_ tile, grid [][]tile, visited []tile) []tile {
	neighbours := []tile{}
	for _, path := range tile_.tileType.paths {
		switch path.out {
		case RIGHT:
			if len(grid[0]) > tile_.x+1 {
				next := grid[tile_.y][tile_.x+1]
				if !contains(visited, next) {
					neighbours = append(neighbours, next)
				}
			}
		case LEFT:
			if tile_.x-1 > -1 {
				next := grid[tile_.y][tile_.x-1]
				if !contains(visited, next) {
					neighbours = append(neighbours, next)
				}
			}
		case BOTTOM:
			if len(grid) > tile_.y+1 {
				next := grid[tile_.y+1][tile_.x]
				if !contains(visited, next) {
					neighbours = append(neighbours, next)
				}
			}
		}
	}
	return neighbours
}

func getNeighForType4BOTTOM(neighs []tile, y int) (tile, error) {
	for _, neigh := range neighs {
		if neigh.y > y {
			return neigh, nil
		}
	}
	return tile{}, errors.New("no tile found")
}

func getNeighForType4LEFT(neighs []tile, x int) (tile, error) {
	for _, neigh := range neighs {
		if neigh.x < x {
			return neigh, nil
		}
	}
	return tile{}, errors.New("no tile found")
}

func getNeighForType5BOTTOM(neighs []tile, y int) (tile, error) {
	for _, neigh := range neighs {
		if neigh.y > y {
			return neigh, nil
		}
	}
	return tile{}, errors.New("no tile found")
}

func getNeighForType5RIGHT(neighs []tile, x int) (tile, error) {
	for _, neigh := range neighs {
		if neigh.x > x {
			return neigh, nil
		}
	}
	return tile{}, errors.New("no tile found")
}

func getBestPosibleNeighbour(neighs []tile, curr tile, last tile) tile {
	if curr.tileType.id == 4 && last.x > curr.x {
		fmt.Fprint(os.Stderr, "Moin4")

		ret, err := getNeighForType4BOTTOM(neighs, curr.y)
		if err == nil {
			return ret
		}
	}
	if curr.tileType.id == 4 && last.y < curr.y {
		fmt.Fprint(os.Stderr, "Moin3")

		ret, err := getNeighForType4LEFT(neighs, curr.x)
		if err == nil {
			return ret
		}
	}
	if curr.tileType.id == 5 && last.x < curr.x {
		fmt.Fprint(os.Stderr, "Moin")
		ret, err := getNeighForType5BOTTOM(neighs, curr.y)
		if err == nil {
			return ret
		}
	}
	if curr.tileType.id == 5 && last.y < curr.y {
		fmt.Fprint(os.Stderr, "Moin2")

		ret, err := getNeighForType5RIGHT(neighs, curr.x)
		if err == nil {
			return ret
		}
	}
	fmt.Fprint(os.Stderr, curr.tileType.id)
	fmt.Fprint(os.Stderr, last.tileType.id)
	return neighs[0]
}

func getNextTile(curr tile, grid [][]tile, start bool, visited []tile, last tile) (tile, tile) {
	if start {
		for _, tile := range grid[0] {
			for _, path := range tile.tileType.paths {
				if path.in == TOP {
					return tile, curr
				}
			}
		}
	}
	neighbours := getNeighbours(curr, grid, visited)
	ret := getBestPosibleNeighbour(neighbours, curr, last)
	return ret, curr
}

func getTile(tileTypes []tileType, lineTile int, x int, y int) (tile, error) {
	for _, tileType := range tileTypes {
		if tileType.id == lineTile {
			return tile{
				tileType,
				x,
				y,
			}, nil
		}
	}
	return tile{}, errors.New("no tile found")
}

func main() {

	tileTypes := []tileType{
		{[]path{}, 0},
		{[]path{
			{TOP, BOTTOM},
			{LEFT, BOTTOM},
			{RIGHT, BOTTOM},
		}, 1},
		{[]path{
			{LEFT, RIGHT},
			{RIGHT, LEFT},
		}, 2},
		{[]path{
			{TOP, BOTTOM},
		}, 3},
		{[]path{
			{TOP, LEFT},
			{RIGHT, BOTTOM},
		}, 4},
		{[]path{
			{TOP, RIGHT},
			{LEFT, BOTTOM},
		}, 5},
		{[]path{
			{RIGHT, LEFT},
			{LEFT, RIGHT},
		}, 6},
		{[]path{
			{TOP, BOTTOM},
			{RIGHT, BOTTOM},
		}, 7},
		{[]path{
			{LEFT, BOTTOM},
			{RIGHT, BOTTOM},
		}, 8},
		{[]path{
			{TOP, BOTTOM},
			{LEFT, BOTTOM},
		}, 9},
		{[]path{
			{TOP, LEFT},
		}, 10},
		{[]path{
			{TOP, RIGHT},
		}, 11},
		{[]path{
			{RIGHT, BOTTOM},
		}, 12},
		{[]path{
			{LEFT, BOTTOM},
		}, 13},
	}

	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1000000), 1000000)

	// W: number of columns.
	// H: number of rows.
	var W, H int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &W, &H)

	var grid [][]tile

	for i := 0; i < H; i++ {
		scanner.Scan()
		LINE := strings.Split(scanner.Text(), " ")
		var line []tile
		for j := 0; j < W; j++ {
			lineTile, _ := strconv.Atoi(LINE[j])
			tile, err := getTile(tileTypes, lineTile, j, i)
			if err == nil {
				line = append(line, tile)
			}
		}
		grid = append(grid, line)
	}

	//for _, tile := range grid {
	//	fmt.Fprintln(os.Stderr, tile)
	//}
	// EX: the coordinate along the X axis of the exit (not useful for this first mission, but must be read).
	var EX int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &EX)

	visited := []tile{}
	curr, last := getNextTile(tile{}, grid, true, visited, tile{})
	for {
		var XI, YI int
		var POS string
		scanner.Scan()
		fmt.Sscan(scanner.Text(), &XI, &YI, &POS)
		// fmt.Fprintln(os.Stderr, "Debug messages...")
		curr, last = getNextTile(curr, grid, false, visited, last)

		visited = append(visited, curr)
		//fmt.Fprintln(os.Stderr, curr)
		// One line containing the X Y coordinates of the room in which you believe Indy will be on the next turn.
		fmt.Printf("%d %d\n", curr.x, curr.y)

	}
}
