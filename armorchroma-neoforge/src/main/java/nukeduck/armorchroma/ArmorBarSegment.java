package nukeduck.armorchroma;

import nukeduck.armorchroma.config.ArmorIcon;

public class ArmorBarSegment {
   private int armorPoints;
   private final ArmorIcon icon;
   private final ArmorIcon leadingMask;
   private final ArmorIcon trailingMask;
   private final boolean hasGlint;

   public ArmorBarSegment(int armorPoints, ArmorIcon icon, ArmorIcon leadingMask, ArmorIcon trailingMask, boolean hasGlint) {
      this.armorPoints = armorPoints;
      this.icon = icon;
      this.leadingMask = leadingMask;
      this.trailingMask = trailingMask;
      this.hasGlint = hasGlint;
   }

   public int getArmorPoints() {
      return this.armorPoints;
   }

   public void setArmorPoints(int armorPoints) {
      this.armorPoints = armorPoints;
   }

   public void addArmorPoints(int delta) {
      this.armorPoints += delta;
   }

   public ArmorIcon getIcon() {
      return this.icon;
   }

   public ArmorIcon getLeadingMask() {
      return this.leadingMask;
   }

   public ArmorIcon getTrailingMask() {
      return this.trailingMask;
   }

   public boolean hasGlint() {
      return this.hasGlint;
   }

   public boolean canMergeWith(ArmorBarSegment segment) {
      return this.hasGlint == segment.hasGlint
         && this.icon.equals(segment.icon)
         && this.leadingMask.equals(segment.leadingMask)
         && this.trailingMask.equals(segment.trailingMask);
   }
}
