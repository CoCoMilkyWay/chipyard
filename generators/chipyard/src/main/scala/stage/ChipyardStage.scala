// See LICENSE for license details.
// Based on Rocket Chip's stage implementation

package chipyard.stage

import chisel3.stage.{ChiselCli, ChiselStage}
import firrtl.options.PhaseManager.PhaseDependency
import firrtl.options.{Phase, PreservesAll, Shell}
import firrtl.stage.FirrtlCli
import freechips.rocketchip.stage.RocketChipCli
import freechips.rocketchip.system.RocketChipStage

import firrtl.options.{Phase, PhaseManager, PreservesAll, Shell, Stage, StageError, StageMain}
import firrtl.options.phases.DeletedWrapper

class ChipyardStage extends ChiselStage with PreservesAll[Phase] {
  override val shell = new Shell("chipyard") with ChipyardCli with RocketChipCli with ChiselCli with FirrtlCli
  // So this does work, but it requires a series of dependency fixes. For now,
  // copy the list from RC into our stage.
  // override val targets: Seq[PhaseDependency] = classOf[chipyard.stage.phases.AddDefaultTests] +: (new RocketChipStage).targets
  // Changes:
  // 1) RC: TestSuiteAnnotation should be marked as Unserializable, or
  //    AddDefaultTests must name GenerateFirrtlAnnos as a prereq to prevent
  //    accidental serialization
  // 2) RC: TransformAnnotations must name AddImplicitOutputFile as as a
  //    dependent (or Checks) Intuitively, it's not so much that the latter depends on the
  //    prior, but rather the prior must "run before" the latter)
  // 3) Chisel: Emitter must name Convert as a dependent (read: "must run before")

  override val targets: Seq[PhaseDependency] = Seq(
    classOf[freechips.rocketchip.stage.phases.Checks],
    classOf[freechips.rocketchip.stage.phases.TransformAnnotations],
    classOf[freechips.rocketchip.stage.phases.PreElaboration],
    classOf[chisel3.stage.phases.Checks],
    classOf[chisel3.stage.phases.Elaborate],
    classOf[freechips.rocketchip.stage.phases.GenerateROMs],
    classOf[chisel3.stage.phases.AddImplicitOutputFile],
    classOf[chisel3.stage.phases.AddImplicitOutputAnnotationFile],
    classOf[chisel3.stage.phases.MaybeAspectPhase],
    classOf[chisel3.stage.phases.Emitter],
    classOf[chisel3.stage.phases.Convert],
    classOf[freechips.rocketchip.stage.phases.GenerateFirrtlAnnos],
    classOf[freechips.rocketchip.stage.phases.AddDefaultTests],
    // This is the only injected phase
    classOf[chipyard.stage.phases.AddDefaultTests],
    classOf[chipyard.stage.phases.GenerateTestSuiteMakefrags],
    classOf[freechips.rocketchip.stage.phases.GenerateArtefacts],
  )
}
