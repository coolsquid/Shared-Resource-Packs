package coolsquid.sharedresourcepacks.asm;

import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions("coolsquid.sharedresourcepacks.asm")
@IFMLLoadingPlugin.Name("Shared Resource Packs")
public class Transformer implements IFMLLoadingPlugin, IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (transformedName.equals("net.minecraft.client.resources.ResourcePackRepository")) {
			ClassNode c = createClassNode(bytes);
			MethodNode m = getMethod(c, "updateRepositoryEntriesAll", "()V", "b", "()V");
			InsnList l = new InsnList();
			l.add(new VarInsnNode(Opcodes.ALOAD, 0));
			l.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Hooks.class),
					"updateRepositoryEntriesAll", "(Lnet/minecraft/client/resources/ResourcePackRepository;)V", false));
			m.instructions.insertBefore(m.instructions.getLast().getPrevious(), l);
			return toBytes(c);
		}
		return bytes;
	}

	private MethodNode getMethod(ClassNode c, String name, String desc, String obfName, String obfDesc) {
		for (MethodNode m: c.methods) {
			if ((m.name.equals(name) || m.name.equals(obfName)) && (m.desc.equals(desc) || m.desc.equals(obfDesc))) {
				return m;
			}
		}
		return null;
	}

	private static ClassNode createClassNode(byte[] bytes) {
		ClassNode c = new ClassNode();
		ClassReader r = new ClassReader(bytes);
		r.accept(c, ClassReader.EXPAND_FRAMES);
		return c;
	}

	private static byte[] toBytes(ClassNode c) {
		ClassWriter w = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		c.accept(w);
		return w.toByteArray();
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { Transformer.class.getName() };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}