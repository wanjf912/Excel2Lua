local role_cfg2 = {
	[1] = {
		key = "1",
		name = "路人甲",
		skill = "大保健",
		hp = 100.05,
		mp = 20,
		actions = {
			[1] = {
				key = 1,
				framecount = 22,
				png = "dead.png",
				framefmt = "action0_%d_.png",
				plists =  {
					"1.plist", 
					"2.plist", 
					"3.plist", 
					
				},
				actionname = "dead",
			},

			[2] = {
				key = 2,
				framecount = 34,
				png = "walk.png",
				framefmt = "action1_%d_.png",
				plists =  {
					"4.plist", 
					"5.plist", 
					"6.plist", 
					
				},
				actionname = "walk",
			},

			[3] = {
				key = 3,
				framecount = 22,
				png = "attack.png",
				framefmt = "action2_%d_.png",
				plists =  {
					"1.plist", 
					"2.plist", 
					"3.plist", 
					
				},
				actionname = "attack",
			},

		},
		defense = true,
		items =  {
			"1", 
			"item1", 
			"item2", 
			"itme3", 
			
		},
		randomNum =  {
			1,
			2,
			4,
			5,
			
		},
	},

	[2] = {
		key = "2",
		name = "路人乙",
		skill = "大宝剑",
		hp = 100.0,
		mp = 20,
		actions = {
			[1] = {
				key = 4,
				framecount = 34,
				png = "idle.png",
				framefmt = "action3_%d_.png",
				plists =  {
					"4.plist", 
					"5.plist", 
					"6.plist", 
					
				},
				actionname = "idle",
			},

			[2] = {
				key = 5,
				framecount = 22,
				png = "sleep.png",
				framefmt = "action4_%d_.png",
				plists =  {
					"1.plist", 
					"2.plist", 
					"3.plist", 
					
				},
				actionname = "sleep",
			},

			[3] = {
				key = 6,
				framecount = 34,
				png = "wake.png",
				framefmt = "action5_%d_.png",
				plists =  {
					"4.plist", 
					"5.plist", 
					"6.plist", 
					
				},
				actionname = "wake",
			},

		},
		defense = false,
		items =  {
			"1", 
			"item4", 
			"item2", 
			"itme5", 
			
		},
		randomNum =  {
			3,
			5,
			6,
			7,
			
		},
	},

}

local Sheet1 = {
	[1] = {
		key = 1,
		framecount = 22,
		png = "dead.png",
		framefmt = "action0_%d_.png",
		plists =  {
			"1.plist", 
			"2.plist", 
			"3.plist", 
			
		},
		actionname = "dead",
	},

	[2] = {
		key = 2,
		framecount = 34,
		png = "walk.png",
		framefmt = "action1_%d_.png",
		plists =  {
			"4.plist", 
			"5.plist", 
			"6.plist", 
			
		},
		actionname = "walk",
	},

	[3] = {
		key = 3,
		framecount = 22,
		png = "attack.png",
		framefmt = "action2_%d_.png",
		plists =  {
			"1.plist", 
			"2.plist", 
			"3.plist", 
			
		},
		actionname = "attack",
	},

	[4] = {
		key = 4,
		framecount = 34,
		png = "idle.png",
		framefmt = "action3_%d_.png",
		plists =  {
			"4.plist", 
			"5.plist", 
			"6.plist", 
			
		},
		actionname = "idle",
	},

	[5] = {
		key = 5,
		framecount = 22,
		png = "sleep.png",
		framefmt = "action4_%d_.png",
		plists =  {
			"1.plist", 
			"2.plist", 
			"3.plist", 
			
		},
		actionname = "sleep",
	},

	[6] = {
		key = 6,
		framecount = 34,
		png = "wake.png",
		framefmt = "action5_%d_.png",
		plists =  {
			"4.plist", 
			"5.plist", 
			"6.plist", 
			
		},
		actionname = "wake",
	},

}

local Sheet2 = {
	[1] = {
		key = 5,
		framecount = 22,
		png = "sleep.png",
		framefmt = "action4_%d_.png",
		plists =  {
			"1.plist", 
			"2.plist", 
			"3.plist", 
			
		},
		actionname = "sleep",
	},

	[2] = {
		key = 6,
		framecount = 34,
		png = "wake.png",
		framefmt = "action5_%d_.png",
		plists =  {
			"4.plist", 
			"5.plist", 
			"6.plist", 
			
		},
		actionname = "wake",
	},

}


return role_cfg2, Sheet1, Sheet2
