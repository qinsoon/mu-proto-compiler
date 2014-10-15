; ModuleID = 'int-register-spilling-nospill.c'
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.9.0"

; Function Attrs: nounwind ssp uwtable
define i32 @main() #0 {
  %i = alloca i32, align 4
  store volatile i32 0, i32* %i, align 4
  %1 = load volatile i32* %i, align 4
  %2 = icmp slt i32 %1, 10
  br i1 %2, label %.lr.ph, label %._crit_edge

.lr.ph:                                           ; preds = %0, %.lr.ph
  %v1.04 = phi i32 [ %4, %.lr.ph ], [ 0, %0 ]
  %v4.03 = phi i32 [ %7, %.lr.ph ], [ 0, %0 ]
  %v3.02 = phi i32 [ %6, %.lr.ph ], [ 0, %0 ]
  %v2.01 = phi i32 [ %5, %.lr.ph ], [ 0, %0 ]
  %3 = load volatile i32* %i, align 4
  %4 = add nsw i32 %3, %v1.04
  %5 = add nsw i32 %4, %v2.01
  %6 = add nsw i32 %5, %v3.02
  %7 = add nsw i32 %6, %v4.03
  %8 = load volatile i32* %i, align 4
  %9 = add nsw i32 %8, 1
  store volatile i32 %9, i32* %i, align 4
  %10 = load volatile i32* %i, align 4
  %11 = icmp slt i32 %10, 10
  br i1 %11, label %.lr.ph, label %._crit_edge

._crit_edge:                                      ; preds = %.lr.ph, %0
  %v1.0.lcssa = phi i32 [ 0, %0 ], [ %4, %.lr.ph ]
  %v4.0.lcssa = phi i32 [ 0, %0 ], [ %7, %.lr.ph ]
  %v3.0.lcssa = phi i32 [ 0, %0 ], [ %6, %.lr.ph ]
  %v2.0.lcssa = phi i32 [ 0, %0 ], [ %5, %.lr.ph ]
  %12 = add i32 %v3.0.lcssa, %v2.0.lcssa
  %13 = add i32 %12, %v4.0.lcssa
  %14 = add i32 %13, %v1.0.lcssa
  %15 = srem i32 %14, 125
  ret i32 %15
}

attributes #0 = { nounwind ssp uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = metadata !{metadata !"Apple LLVM version 6.0 (clang-600.0.51) (based on LLVM 3.5svn)"}
