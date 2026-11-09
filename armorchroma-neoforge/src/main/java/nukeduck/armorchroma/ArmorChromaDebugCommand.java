package nukeduck.armorchroma;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ArmorChromaDebugCommand {
   private static final DynamicCommandExceptionType FAILED_ITEMLESS_EXCEPTION = new DynamicCommandExceptionType(
      entityName -> Component.translatable("commands.enchant.failed.itemless", entityName)
   );

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("armorchroma")
            .then(
               Commands.literal("setarmor")
                  .then(Commands.argument("points", DoubleArgumentType.doubleArg()).executes(ArmorChromaDebugCommand::executeSetArmor))
                  .then(Commands.literal("reset").executes(ArmorChromaDebugCommand::executeResetArmor))
            )
            .then(
               Commands.literal("setglint")
                  .then(Commands.argument("glint", BoolArgumentType.bool()).executes(ArmorChromaDebugCommand::executeSetGlint))
                  .then(Commands.literal("reset").executes(ArmorChromaDebugCommand::executeResetGlint))
            )
      );
   }

   private static int executeSetArmor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ItemStack stack = getActiveStack(context);
      double points = DoubleArgumentType.getDouble(context, "points");
      ItemAttributeModifiers originalModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
      List<ItemAttributeModifiers.Entry> modifiers;
      if (originalModifiers == null) {
         modifiers = new ArrayList<>(1);
      } else {
         modifiers = originalModifiers.modifiers()
            .stream()
            .filter(entry -> entry.attribute() != Attributes.ARMOR)
            .collect(Collectors.toCollection(ArrayList::new));
      }

      AttributeModifier attributeModifier = new AttributeModifier(
         ResourceLocation.fromNamespaceAndPath("armorchroma", UUID.randomUUID().toString()),
         points,
         AttributeModifier.Operation.ADD_VALUE
      );
      modifiers.add(new ItemAttributeModifiers.Entry(Attributes.ARMOR, attributeModifier, EquipmentSlotGroup.ANY));
      stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(modifiers));
      return 1;
   }

   private static int executeResetArmor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ItemStack stack = getActiveStack(context);
      ItemAttributeModifiers oldModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
      if (oldModifiers != null) {
         List<ItemAttributeModifiers.Entry> modifiers = oldModifiers.modifiers()
            .stream()
            .filter(entry -> entry.attribute() != Attributes.ARMOR || !entry.modifier().id().getNamespace().equals("armorchroma"))
            .toList();
         stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(modifiers));
      }

      return 1;
   }

   private static int executeSetGlint(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ItemStack stack = getActiveStack(context);
      boolean glint = BoolArgumentType.getBool(context, "glint");
      stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
      return 1;
   }

   private static int executeResetGlint(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      getActiveStack(context).remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
      return 1;
   }

   private static ItemStack getActiveStack(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
      ServerPlayer player = context.getSource().getPlayerOrException();
      ItemStack stack = player.getMainHandItem();
      if (stack.isEmpty()) {
         throw FAILED_ITEMLESS_EXCEPTION.create(player.getName().getString());
      } else {
         return stack;
      }
   }
}
