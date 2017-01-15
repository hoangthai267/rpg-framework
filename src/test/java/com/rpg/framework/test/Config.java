package com.rpg.framework.test;

public class Config {
	public final static int CHARACTER_DAMAGE 			= 5;
	public final static int CHARACTER_DEFENSE	 		= 2;
	public final static int CHARACTER_SPEED 			= 3;
	public final static int CHARACTER_NUMBER_OF_ATTACK 	= 3;
	public final static int CHARACTER_HP		 		= 100;
	public final static int CHARACTER_MP		 		= 100;
	public final static int CHARACTER_EXP				= 100;
	public final static int CHARACTER_START_MAP_1_X     = -3;
	public final static int CHARACTER_START_MAP_1_Y     = 0;
	// Change the number of monster type to get more.
	public final static int NUMBER_OF_MONSTER_TYPE 		= 3;

	public final static int MONSTER_TYPE_1_DAMAGE 		= 15;
	public final static int MONSTER_TYPE_1_DEFENSE 		= 1;
	public final static int MONSTER_TYPE_1_SPEED 		= 1;
	public final static int MONSTER_TYPE_1_HP 			= 15;
	public final static int MONSTER_TYPE_1_MP 			= 10;
	
	public final static int MONSTER_TYPE_2_DAMAGE 		= 2;
	public final static int MONSTER_TYPE_2_DEFENSE 		= 2;
	public final static int MONSTER_TYPE_2_SPEED 		= 2;
	public final static int MONSTER_TYPE_2_HP 			= 20;
	public final static int MONSTER_TYPE_2_MP 			= 20;
	
	public final static int MONSTER_TYPE_3_DAMAGE 		= 3;
	public final static int MONSTER_TYPE_3_DEFENSE 		= 3;
	public final static int MONSTER_TYPE_3_SPEED 		= 3;
	public final static int MONSTER_TYPE_3_HP 			= 30;
	public final static int MONSTER_TYPE_3_MP 			= 30;
	
	
	
	public final static int MONSTER_DAMAMGE[] = new int[] {
			MONSTER_TYPE_1_DAMAGE, 
			MONSTER_TYPE_2_DAMAGE,
			MONSTER_TYPE_3_DAMAGE
			// add more here
			};
	
	public final static int MONSTER_DEFENSE[] = new int[] {
			MONSTER_TYPE_1_DEFENSE, 
			MONSTER_TYPE_2_DEFENSE,
			MONSTER_TYPE_3_DEFENSE
			// add more here
			};
	
	public final static int MONSTER_SPEED[] = new int[] {
			MONSTER_TYPE_1_SPEED, 
			MONSTER_TYPE_2_SPEED,
			MONSTER_TYPE_3_SPEED
			// add more here
			};
	
	public final static int MONSTER_HP[] = new int[] {
			MONSTER_TYPE_1_HP, 
			MONSTER_TYPE_2_HP,
			MONSTER_TYPE_3_HP
			// add more here
			};
	
	public final static int MONSTER_MP[] = new int[] {
			MONSTER_TYPE_1_MP, 
			MONSTER_TYPE_2_MP,
			MONSTER_TYPE_3_MP
			// add more here
			};
	
	public final static double MAP1_SNAIL[] = new double[] {
//			-2.38, -2.21,
//			-0.24, -2.21,
//			2.58, -2.21,
//			7.53, -2.21,
//			10.98, -2.21,
			13.99, -2.99,
			15.58, 0.88,
//			18.27, 1.54,
//			20.23, 1.48,

	};
}
