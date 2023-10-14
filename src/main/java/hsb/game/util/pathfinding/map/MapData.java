package hsb.game.util.pathfinding.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 地图数据
 *
 * @author U-Demon
 */
public class MapData {
	
	/** 文件来源于：https://github.com/SteveRabin/JPSPlusWithGoalBounding/blob/master/JPSPlusGoalBounding/Maps/maze-100-1.map */
	private static final String FILE_NAME = "G:\\kaifa_environment\\code\\java\\PathFinding\\java\\maps\\maze-100-3.map";
	
	/** 阻挡标志量 */
	public static final byte OBSTACLE = 0;
	public static final byte ACCESS = 1;
	
//private byte[][] datas;
	private byte[] datas;
	private int height = 4198;
	private int width = 4195;
	
	public MapData(byte[] datas) {
		if (datas!=null){
			this.datas =datas;
		}else {
			datas = new byte[height*width];
		}

	}
	
	private void init() {

	}
	
	/**
	 * 是否可行走
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isWalkable(int x, int y) {
		if (x < 0 || x >= height || y < 0 || y >= width)
			return false;
		//return datas[x][y] == ACCESS;
		int index = x+y*width;
		return datas[index] == ACCESS;
	}


	
}
