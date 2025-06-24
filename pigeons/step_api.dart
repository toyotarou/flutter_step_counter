import 'package:pigeon/pigeon.dart';

/// 歩数レコードモデル（KotlinのStepRecordに相当）
class StepRecord {
  late String date;
  late int segment;
  late String time;
  late int step;
}

/// Kotlin → Flutter のデータ取得用API
@HostApi()
abstract class StepApi {
  /// 現在の歩数を取得（センサー値）
  int getCurrentStep();

  /// 保存された歩数レコードの一覧を取得
  List<StepRecord> getAllStepRecords();
}
