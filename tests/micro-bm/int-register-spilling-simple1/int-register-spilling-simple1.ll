; ModuleID = 'int-register-spilling-simple1.c'
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
  %v1.016 = phi i32 [ %4, %.lr.ph ], [ 0, %0 ]
  %v16.015 = phi i32 [ %19, %.lr.ph ], [ 0, %0 ]
  %v15.014 = phi i32 [ %18, %.lr.ph ], [ 0, %0 ]
  %v14.013 = phi i32 [ %17, %.lr.ph ], [ 0, %0 ]
  %v13.012 = phi i32 [ %16, %.lr.ph ], [ 0, %0 ]
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
  %4 = add nsw i32 %3, %v1.016
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
  %16 = add nsw i32 %15, %v13.012
  %17 = add nsw i32 %16, %v14.013
  %18 = add nsw i32 %17, %v15.014
  %19 = add nsw i32 %18, %v16.015
  %20 = load volatile i32* %i, align 4
  %21 = add nsw i32 %20, 1
  store volatile i32 %21, i32* %i, align 4
  %22 = load volatile i32* %i, align 4
  %23 = icmp slt i32 %22, 10
  br i1 %23, label %.lr.ph, label %._crit_edge

._crit_edge:                                      ; preds = %.lr.ph, %0
  %v1.0.lcssa = phi i32 [ 0, %0 ], [ %4, %.lr.ph ]
  %v16.0.lcssa = phi i32 [ 0, %0 ], [ %19, %.lr.ph ]
  %v15.0.lcssa = phi i32 [ 0, %0 ], [ %18, %.lr.ph ]
  %v14.0.lcssa = phi i32 [ 0, %0 ], [ %17, %.lr.ph ]
  %v13.0.lcssa = phi i32 [ 0, %0 ], [ %16, %.lr.ph ]
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
  %24 = add i32 %v3.0.lcssa, %v2.0.lcssa
  %25 = add i32 %24, %v4.0.lcssa
  %26 = add i32 %25, %v5.0.lcssa
  %27 = add i32 %26, %v6.0.lcssa
  %28 = add i32 %27, %v7.0.lcssa
  %29 = add i32 %28, %v8.0.lcssa
  %30 = add i32 %29, %v9.0.lcssa
  %31 = add i32 %30, %v10.0.lcssa
  %32 = add i32 %31, %v11.0.lcssa
  %33 = add i32 %32, %v12.0.lcssa
  %34 = add i32 %33, %v13.0.lcssa
  %35 = add i32 %34, %v14.0.lcssa
  %36 = add i32 %35, %v15.0.lcssa
  %37 = add i32 %36, %v16.0.lcssa
  %38 = add i32 %37, %v1.0.lcssa
  ret i32 %38
}

attributes #0 = { nounwind ssp uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = metadata !{metadata !"Apple LLVM version 5.1 (clang-503.0.40) (based on LLVM 3.4svn)"}
