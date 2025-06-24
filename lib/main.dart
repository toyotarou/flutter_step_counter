import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_step_counter/pigeon/step_api.dart';

void main() {
  runApp(const StepApp());
}

class StepApp extends StatefulWidget {
  const StepApp({super.key});

  @override
  State<StepApp> createState() => _StepAppState();
}

class _StepAppState extends State<StepApp> {
  final StepApi _api = StepApi();
  bool _isRunning = false;
  List<StepRecord?> _records = [];

  static const int updateIntervalSeconds = 60;
  int _remainingSeconds = updateIntervalSeconds;

  Timer? _updateTimer;
  Timer? _countdownTimer;

  @override
  void initState() {
    super.initState();
    _loadData();
    _startTimers();
  }

  void _startTimers() {
    // ✅ 毎分データ更新
    _updateTimer = Timer.periodic(const Duration(seconds: updateIntervalSeconds), (_) {
      _loadData();
      setState(() => _remainingSeconds = updateIntervalSeconds); // カウントリセット
    });

    // ✅ 毎秒カウントダウン
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (_remainingSeconds > 0) {
        setState(() {
          _remainingSeconds--;
        });
      }
    });
  }

  @override
  void dispose() {
    _updateTimer?.cancel();
    _countdownTimer?.cancel();
    super.dispose();
  }

  Future<void> _loadData() async {
    try {
      final running = await _api.isServiceRunning();
      final records = await _api.getAllStepRecords();

      setState(() {
        _isRunning = running;
        _records = records;
      });

      print("📥 records count: ${_records.length}");
    } catch (e, stack) {
      debugPrint('❌ _loadData 失敗: $e');
      debugPrint(stack.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text("👟 歩数記録 & サービス状態")),
        body: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              // 🔧 サービス稼働状態表示
              Row(
                children: [
                  const Text("🛠 サービス状態: ", style: TextStyle(fontSize: 18)),
                  Text(
                    _isRunning ? "✅ 稼働中" : "⛔️ 停止中",
                    style: TextStyle(
                      fontSize: 18,
                      color: _isRunning ? Colors.green : Colors.red,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const Spacer(),
                  ElevatedButton(onPressed: _loadData, child: const Text("🔄 更新")),
                ],
              ),
              const SizedBox(height: 8),
              Text("⏳ 次の自動更新まで: $_remainingSeconds 秒", style: const TextStyle(fontSize: 16)),
              const SizedBox(height: 16),
              const Divider(),

              // 📋 リスト表示
              const Text("📋 歩数レコード一覧", style: TextStyle(fontSize: 20)),
              const SizedBox(height: 8),
              Expanded(
                child: _records.isEmpty
                    ? const Center(child: Text("データがありません"))
                    : ListView.builder(
                        itemCount: _records.length,
                        itemBuilder: (context, index) {
                          final r = _records[index];
                          if (r == null) return const SizedBox.shrink();
                          return ListTile(
                            leading: const Icon(Icons.directions_walk),
                            title: Text(r.date),
                            subtitle: Text("歩数: ${r.step}｜時間: ${r.time}"),
                          );
                        },
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
