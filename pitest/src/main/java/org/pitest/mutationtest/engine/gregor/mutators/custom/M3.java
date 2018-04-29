package org.pitest.mutationtest.engine.gregor.mutators.custom;

import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum M3 implements MethodMutatorFactory
{

    M3_MUTATOR;

    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new M3MethodVisitor(this, context, methodVisitor);
    }

    @Override
    public String getGloballyUniqueId() {
        return getClass().getName();
    }

    @Override
    public String getName() { return "M3_MUTATOR";}

}

class MethodList extends ClassVisitor
{
    final String m_name;
    final ArrayList<MethodInfo> m_list = new ArrayList<>();

    public MethodList(final String m_name)
    {
        super(Opcodes.ASM6);
        this.m_name = m_name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        m_list.add(new MethodInfo().withAccess(access).withMethodName(name).withMethodDescriptor(desc));
        return null;
    }
}


class M3MethodVisitor extends MethodVisitor {

    final MutationContext context;
    final MethodMutatorFactory factory;

    public M3MethodVisitor(final MethodMutatorFactory factory, final MutationContext context, final MethodVisitor writer)//, final int key)
    {
        super(Opcodes.ASM6, writer);
        this.factory = factory;
        this.context = context;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf)
    {
            if (shouldMutate(name,desc)) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }

    }

    private ArrayList<MethodInfo> listMethods(final String owner, final String name, final String desc) {

        ClassReader cr;
        try
        {
            cr = new ClassReader(owner);
            return findOverloaded(owner, name, desc, cr);
        }
        catch (IOException ex)
        {
        }
        return new ArrayList<>();
    }

    private ArrayList<MethodInfo> findOverloaded(final String owner, final String name, final String desc, final ClassReader cr)
    {
        MethodList ml = new MethodList(name);
        cr.accept(ml, 0);
        return filterOverloaded(name, desc, ml.m_list);
    }

    private ArrayList<MethodInfo> filterOverloaded(final String name, final String desc, final ArrayList<MethodInfo> methodlist)
    {
        ArrayList<MethodInfo> filtered = new ArrayList<>();
        MethodInfo invoked =null;
        Type[] oldArgTypes = Type.getArgumentTypes(desc);

        for (MethodInfo curr : methodlist)
        {
            if (!name.equals(curr.getName()) && desc.equals(curr.getMethodDescriptor()))
            {
                invoked = curr;
            }
        }

        for (MethodInfo curr : methodlist)
        {

            /**
             * Method names are not the same, but the argument types and return type match.
             */

            Type[] newArgTypes = Type.getArgumentTypes(curr.getMethodDescriptor());
            if ((!invoked.getName().equals(curr.getName()))
                    && invoked.getReturnType().equals(curr.getReturnType())
                    && invoked.getAccess() == curr.getAccess()
                    && newArgTypes.length == oldArgTypes.length
                    && similarArgTypes(oldArgTypes, newArgTypes)) {
                filtered.add(curr);
            }
        }
        return filtered;
    }

    private boolean shouldMutate(final String origname, final String Desc)
    {
        final MutationIdentifier newId = this.context.registerMutation(this.factory, "M3 Mutation");
        return this.context.shouldMutate(newId);
    }

    private boolean similarArgTypes(Type[] list1, Type[] list2) {
        for (int i = 0; i < list1.length; i++) {
            if (list2.length <= i) {
                return true;
            }
            if (!list1[i].equals(list2[i])) {
                return false;
            }
        }
        return true;
    }
}
