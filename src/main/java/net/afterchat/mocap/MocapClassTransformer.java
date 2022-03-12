/**
 * The Mocap mod is a Minecraft mod aimed at film-makers who wish to
 * record the motion of their character and replay it back via a scripted
 * entity. It allows you to create a whole series of composite characters
 * for use in your own videos without having to splash out on extra accounts
 * or enlist help.
 * 
 * Copyright (C) 2013-2014 Echeb Keso
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.*
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.afterchat.mocap;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Iterator;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MocapClassTransformer implements IClassTransformer {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) {

		/* Release Obf class */
		if (arg0.equals("afh")) { //aji -> afh
			logger.info("** MOCAP - Injecting new event trigger into 'Block' Class : "
					+ arg0);
			return patchClassASM(arg0, arg2);
		}

		/* Debug class */
		if (arg0.equals("net.minecraft.block.Block")) {
			logger.info("** MOCAP - Injecting new event trigger into 'Block' Class : "
					+ arg0);
			return patchClassASM(arg0, arg2);
		}
		return arg2;
	}

	public byte[] patchClassASM(String name, byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		Iterator<MethodNode> methods = classNode.methods.iterator();

		
		while (methods.hasNext()) {
			MethodNode m = methods.next();
			
			if ((m.name.equals("onBlockPlacedBy"))
					|| (m.name.equals("a") && m.desc
							.equals("(Ladm;Lcj;Lalz;Lpr;Lzx;)V"))) { // Lahb;IIILsv;Ladd; -> Ladm;Lcj;Lalz;Lpr;Lzx;
				logger.info("** MOCAP - Patching onBlockPlacedBy: " + m.name);
				AbstractInsnNode currentNode = null;
				Iterator<AbstractInsnNode> iter = m.instructions.iterator();
				int index = -1;

				while (iter.hasNext()) {
					index++;
					currentNode = iter.next();

					/**
					 * Just prior to the original empty function return, inject code to trigger
					 * our custom block place event.
					 */
					if (currentNode.getOpcode() == RETURN) {
						InsnList toInject = new InsnList();
						toInject.add(new TypeInsnNode(NEW,
								"net/afterchat/mocap/LivingPlaceBlockEvent"));
						toInject.add(new InsnNode(DUP));
						toInject.add(new VarInsnNode(ALOAD, 4)); // 5 -> 4
						toInject.add(new VarInsnNode(ALOAD, 5)); // 6 -> 5
						/*toInject.add(new VarInsnNode(ILOAD, 2));
						toInject.add(new VarInsnNode(ILOAD, 3));
						toInject.add(new VarInsnNode(ILOAD, 4));*/
						toInject.add(new VarInsnNode(ALOAD, 2)); // BlockPos
						toInject.add(new MethodInsnNode(INVOKESPECIAL,
								"net/afterchat/mocap/LivingPlaceBlockEvent",
								"<init>",
								"(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/BlockPos;)V")); // III ->Lnet/minecraft/util/BlockPos
						toInject.add(new VarInsnNode(ASTORE, 6)); // 7 -> 6
						toInject.add(new FieldInsnNode(GETSTATIC,
								"net/minecraftforge/common/MinecraftForge",
								"EVENT_BUS",
								"Lnet/minecraftforge/fml/common/eventhandler/EventBus;")); // cpw/mods -> net/minecraftforge
						toInject.add(new VarInsnNode(ALOAD, 6)); // 7 -> 6
						toInject.add(new MethodInsnNode(INVOKEVIRTUAL,
								"net/minecraftforge/fml/common/eventhandler/EventBus", // cpw/mods -> net/minecraftforge
								"post",
								"(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z")); // cpw/mods -> net/minecraftforge
						toInject.add(new InsnNode(POP));

						m.instructions.insertBefore(currentNode, toInject);
					}
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS
				| ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
