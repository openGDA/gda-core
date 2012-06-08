/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.generalfilereaders;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Mar345 File reader This is a java conversion of c routines provided my MAR to read MAR 345 Images I found JidImage
 * had problems reading MAR images NOTE: It takes about a minute to read in a MAR345 image file !!!
 */

public class Mar345FormatReader {

	static int setbits[] = new int[] { 0x00000000, 0x00000001, 0x00000003, 0x00000007, 0x0000000F, 0x0000001F,
			0x0000003F, 0x0000007F, 0x000000FF, 0x000001FF, 0x000003FF, 0x000007FF, 0x00000FFF, 0x00001FFF, 0x00003FFF,
			0x00007FFF, 0x0000FFFF, 0x0001FFFF, 0x0003FFFF, 0x0007FFFF, 0x000FFFFF, 0x001FFFFF, 0x003FFFFF, 0x007FFFFF,
			0x00FFFFFF, 0x01FFFFFF, 0x03FFFFFF, 0x07FFFFFF, 0x0FFFFFFF, 0x1FFFFFFF, 0x3FFFFFFF, 0x7FFFFFFF, 0xFFFFFFFF };

	private static int shift_left(int x, int n) {
		return (((x) & setbits[32 - (n)]) << (n));
	}

	private static int shift_right(int x, int n) {
		return (((x) >> (n)) & setbits[32 - (n)]);
	}

	/**
	 * @param fileReader
	 * @param img
	 */
	public static void get_pck(FileReader fileReader, short[] img) {
		int x = 0, y = 0, i = 0;
		int BUFFERSIZE = 8096;
		char[] header = new char[8096];
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		if (fileReader == null)
			return;
		header[0] = '\n';
		header[1] = 0;

		i = 2;
		while (i < BUFFERSIZE) {
			try {
				header[i] = (char) bufferedReader.read();
				if (header[i] == '\n') {
					String current_header = new String(header);
					Scanner s = new Scanner(current_header);
					// THIS LINE CONTAINS THE IMAGE DIMENSIONS
					s.findInLine("CCP4 packed image, X: (\\d+), Y: (\\d+)");
					try {
						MatchResult result = s.match();
						if (result.groupCount() == 2) {
							// The extracted image dimensions
							x = Integer.parseInt(result.group(1));
							y = Integer.parseInt(result.group(2));
							break;
						}

					} catch (IllegalStateException e) {

					}
					s.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			i++;
		}
		// Information on image size not found
		if (x == 0 && y == 0) {
			img = null;
			return;
		}
		// Allocat the image array
		img = new short[x * y - 1];
		// unpack the image data to img
		unpack_word(bufferedReader, x, y, img);
	}

	/*******************************************************************************************************************
	 * Function: unpack_word
	 * 
	 * @param bufferedReader
	 * @param x
	 * @param y
	 * @param img
	 ******************************************************************************************************************/
	public static void unpack_word(BufferedReader bufferedReader, int x, int y, short[] img) {
		int valids = 0, spillbits = 0, usedbits, total = x * y;
		int window = 0, spill = 0, pixel = 0, nextint, bitnum, pixnum;
		int bitdecode[] = new int[] { 0, 4, 5, 6, 7, 8, 16, 32 };

		while (pixel < total) {
			if (valids < 6) {
				if (spillbits > 0) {
					window |= shift_left(spill, valids);
					valids += spillbits;
					spillbits = 0;
				} else {
					try {
						spill = bufferedReader.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
					spillbits = 8;
				}
			} else {
				pixnum = 1 << (window & setbits[3]);
				window = shift_right(window, 3);
				bitnum = bitdecode[window & setbits[3]];
				window = shift_right(window, 3);
				valids -= 6;
				while ((pixnum > 0) && (pixel < total)) {
					if (valids < bitnum) {
						if (spillbits > 0) {
							window |= shift_left(spill, valids);
							if ((32 - valids) > spillbits) {
								valids += spillbits;
								spillbits = 0;
							} else {
								usedbits = 32 - valids;
								spill = shift_right(spill, usedbits);
								spillbits -= usedbits;
								valids = 32;
							}
						} else {
							try {
								spill = bufferedReader.read();
							} catch (IOException e) {
								e.printStackTrace();
							}
							spillbits = 8;
						}
					} else {
						--pixnum;
						if (bitnum == 0)
							nextint = 0;
						else {
							nextint = window & setbits[bitnum];
							valids -= bitnum;
							window = shift_right(window, bitnum);
							if ((nextint & (1 << (bitnum - 1))) != 0)
								nextint |= ~setbits[bitnum];
						}
						if (pixel > x) {
							img[pixel] = (short) (nextint + (img[pixel - 1] + img[pixel - x + 1] + img[pixel - x]
									+ img[pixel - x - 1] + 2) / 4);
							++pixel;
						} else if (pixel != 0) {
							img[pixel] = (short) (img[pixel - 1] + nextint);
							++pixel;
						} else
							img[pixel++] = (short) nextint;
					}
				}
			}
		}
	}

	/**
	 * Open an image file.
	 * 
	 * @param filePath
	 */
	public static void openImage(String filePath) {
		short[] img = null;
		FileReader fr = null;
		try {
			fr = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		get_pck(fr, img);

	}
}