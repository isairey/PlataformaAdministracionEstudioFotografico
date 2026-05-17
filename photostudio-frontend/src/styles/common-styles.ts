export const cardStyles = {
  container: "p-4 cursor-pointer hover:shadow-md transition-shadow",
  contentWrapper: "flex flex-col gap-2",
  headerRow: "flex items-start justify-between gap-1",
  headerTitle: "font-semibold text-md leading-tight text-left",
  textRow: "flex items-center gap-1 text-xs text-muted-foreground",
  icon: "h-3 w-3",
  dialogGrid: "grid gap-4 py-4",
  dialogGridCols: "grid grid-cols-2 gap-4",
  dialogDetailRow: "flex items-center gap-2 gap-1",
  dialogLabel: "text-sm text-muted-foreground",
  dialogValue: "text-sm",
  containerSubText: "text-xs",
  dialogContentSize: "sm:max-w-[500px]",
}
export const buttonStyles = {
    primaryButton: "w-full bg-main-site text-white hover:opacity-90 rounded cursor-pointer",
    primaryButtonSize: "sm" as const,
}

export const baseColors = {
    successColor: "#22c55e",
    failureColor: "#ef4444",

}

export const loaderStyles = {
  inButtonLoader: "mr-2 h-4 w-4 animate-spin",
  mediumLoader: "h-6 w-6 animate-spin text-muted-foreground"
}