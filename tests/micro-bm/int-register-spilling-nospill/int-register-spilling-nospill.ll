; ModuleID = 'int-register-spilling-nospill.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.9.0"

; Function Attrs: nounwind ssp uwtable
define i32 @main() #0 {
  %i = alloca i32, align 4
  store volatile i32 0, i32* %i, align 4
  %1 = load volatile i32* %i, align 4
  %2 = icmp slt i32 %1, 10
  br i1 %2, label %.lr.ph, label %._crit_edge

.lr.ph:                                           ; preds = %0, %.lr.ph
  %v1.012 = phi i32 [ %4, %.lr.ph ], [ 0, %0 ]
  %v12.011 = phi i32 [ %15, %.lr.ph ], [ 0, %0 ]
  %v11.010 = phi i32 [ %14, %.lr.ph ], [ 0, %0 ]
  %v10.09 = phi i32 [ %13, %.lr.ph ], [ 0, %0 ]
  %v9.08 = phi i32 [ %12, %.lr.ph ], [ 0, %0 ]
  %v8.07 = phi i32 [ %11, %.lr.ph ], [ 0, %0 ]
  %v7.06 = phi i32 [ %10, %.lr.ph ], [ 0, %0 ]
  %v6.05 = phi i32 [ %9, %.lr.ph ], [ 0, %0 ]
  %v5.04 = phi i32 [ %8, %.lr.ph ], [ 0, %0 ]
  %v4.03 = phi i32 [ %7, %.lr.ph ], [ 0, %0 ]
  %v3.02 = phi i32 [ %6, %.lr.ph ], [ 0, %0 ]
  %v2.01 = phi i32 [ %5, %.lr.ph ], [ 0, %0 ]
  %3 = load volatile i32* %i, align 4
  %4 = add nsw i32 %3, %v1.012
  %5 = add nsw i32 %4, %v2.01
  %6 = add nsw i32 %5, %v3.02
  %7 = add nsw i32 %6, %v4.03
  %8 = add nsw i32 %7, %v5.04
  %9 = add nsw i32 %8, %v6.05
  %10 = add nsw i32 %9, %v7.06
  %11 = add nsw i32 %10, %v8.07
  %12 = add nsw i32 %11, %v9.08
  %13 = add nsw i32 %12, %v10.09
  %14 = add nsw i32 %13, %v11.010
  %15 = add nsw i32 %14, %v12.011
  %16 = load volatile i32* %i, align 4
  %17 = add nsw i32 %16, 1
  store volatile i32 %17, i32* %i, align 4
  %18 = load volatile i32* %i, align 4
  %19 = icmp slt i32 %18, 10
  br i1 %19, label %.lr.ph, label %._crit_edge

._crit_edge:                                      ; preds = %.lr.ph, %0
  %v1.0.lcssa = phi i32 [ 0, %0 ], [ %4, %.lr.ph ]
  %v12.0.lcssa = phi i32 [ 0, %0 ], [ %15, %.lr.ph ]
  %v11.0.lcssa = phi i32 [ 0, %0 ], [ %14, %.lr.ph ]
  %v10.0.lcssa = phi i32 [ 0, %0 ], [ %13, %.lr.ph ]
  %v9.0.lcssa = phi i32 [ 0, %0 ], [ %12, %.lr.ph ]
  %v8.0.lcssa = phi i32 [ 0, %0 ], [ %11, %.lr.ph ]
  %v7.0.lcssa = phi i32 [ 0, %0 ], [ %10, %.lr.ph ]
  %v6.0.lcssa = phi i32 [ 0, %0 ], [ %9, %.lr.ph ]
  %v5.0.lcssa = phi i32 [ 0, %0 ], [ %8, %.lr.ph ]
  %v4.0.lcssa = phi i32 [ 0, %0 ], [ %7, %.lr.ph ]
  %v3.0.lcssa = phi i32 [ 0, %0 ], [ %6, %.lr.ph ]
  %v2.0.lcssa = phi i32 [ 0, %0 ], [ %5, %.lr.ph ]
  %20 = add i32 %v3.0.lcssa, %v2.0.lcssa
  %21 = add i32 %20, %v4.0.lcssa
  %22 = add i32 %21, %v5.0.lcssa
  %23 = add i32 %22, %v6.0.lcssa
  %24 = add i32 %23, %v7.0.lcssa
  %25 = add i32 %24, %v8.0.lcssa
  %26 = add i32 %25, %v9.0.lcssa
  %27 = add i32 %26, %v10.0.lcssa
  %28 = add i32 %27, %v11.0.lcssa
  %29 = add i32 %28, %v12.0.lcssa
  %30 = add i32 %29, %v1.0.lcssa
  ret i32 %30
}

attributes #0 = { nounwind ssp uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = metadata !{metadata !"Apple LLVM version 5.1 (clang-503.0.40) (based on LLVM 3.4svn)"}
